package com.lalilu.lmusic.ui.drawee

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.util.AttributeSet
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.animation.addListener
import androidx.lifecycle.MutableLiveData
import androidx.palette.graphics.Palette
import coil.load
import com.lalilu.lmusic.utils.StackBlurUtils
import com.lalilu.lmusic.utils.addShadow
import com.lalilu.lmusic.utils.toBitmap
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * 结合StackBlur的自定义ImageView
 */
@AndroidEntryPoint
class BlurImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : AppCompatImageView(context, attrs), CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.IO

    @Inject
    lateinit var stackBlur: StackBlurUtils

    var palette: MutableLiveData<Palette?> = MutableLiveData(null)
    var maxOffset = 0
    private var maxBlurRadius = 50

    @IntRange(from = 0, to = 2000)
    var samplingValue: Int = 400
        set(value) {
            field = value
            launch(Dispatchers.IO) {
                samplingBitmap = createSamplingBitmap(sourceBitmap, value)
                updatePalette(samplingBitmap)
                updateBg(blurRadius)
            }
        }

    @IntRange(from = 0, to = 50)
    var blurRadius = 0
        internal set(value) {
            field = value
            launch(Dispatchers.IO) {
                updateBg(blurRadius)
            }
        }

    @FloatRange(from = 0.0, to = 1.0)
    var blurPercent: Float = 0f
        internal set(value) {
            field = value
            blurRadius = (value * maxBlurRadius).roundToInt()
        }

    @FloatRange(from = 0.0, to = 1.0)
    var dragPercent: Float = 0f

    @FloatRange(from = 0.0, to = 1.0)
    var scalePercent: Float = 0f

    private var bitmapPainter: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var sourceBitmap: Bitmap? = null
    private var samplingBitmap: Bitmap? = null
    private var blurBitmap: Bitmap? = null

    private var sourceRect = Rect()
    private var samplingRect = Rect()
    private var destRect = RectF()

    private var newBitmapPainter: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var newSourceBitmap: Bitmap? = null
    private var newSamplingBitmap: Bitmap? = null
    private var newBlurBitmap: Bitmap? = null

    private var newSourceRect = Rect()
    private var newSamplingRect = Rect()
    private var newDestRect = RectF()

    /**
     * 按照CenterCrop的规则，获取调整后的dest位置
     *
     * @param source Bitmap的大小
     * @param dest 目标位置
     */
    private fun centerCrop(
        source: Rect,
        dest: RectF,
        offsetPercent: Float
    ): RectF {
        val w = source.width().toFloat()
        val h = source.height().toFloat()
        val x = dest.width()
        val y = dest.height()

        val sourceWHRatio = w / h
        val offset: Float

        if (sourceWHRatio > x / y) {
            // 横向长图的情况
            maxOffset = 0
            offset = abs(x - sourceWHRatio * y) / 2
            dest.set(
                dest.left - offset, dest.top,
                x + offset, y
            )
        } else {
            // 竖向长图的情况
            maxOffset = abs(y - x / sourceWHRatio).toInt()
            offset = maxOffset / 2f
            dest.set(
                dest.left, dest.top - offset * (1 - offsetPercent),
                x, y + offset * (1 + offsetPercent)
            )
        }
        return dest
    }

    private val dInterpolator = DecelerateInterpolator()
    private val aInterpolator = AccelerateInterpolator()

    /**
     * 将图片的位置移动至ImageView的中心，并且
     */
    private fun scaleTransform(dest: RectF, scalePercent: Float) {
        val dValue = dInterpolator.getInterpolation(scalePercent)
        val aValue = aInterpolator.getInterpolation(scalePercent)
        val offsetValue = (height.toFloat() - dest.height()) / 2f * dValue
        val scaleValue = 1 + (height.toFloat() / dest.height() - 1) * aValue

        dest.top += offsetValue
        dest.bottom += offsetValue
        scaleX = scaleValue
        scaleY = scaleValue
    }

    override fun onDraw(canvas: Canvas) {
        destRect.set(0f, 0f, width.toFloat(), width.toFloat())
        newDestRect.set(0f, 0f, width.toFloat(), width.toFloat())

        sourceBitmap?.let {
            sourceRect.set(0, 0, it.width, it.height)
            centerCrop(sourceRect, destRect, dragPercent)
            scaleTransform(destRect, scalePercent)
            canvas.drawBitmap(it, sourceRect, destRect, bitmapPainter)
        }
        newSourceBitmap?.let {
            newSourceRect.set(0, 0, it.width, it.height)
            centerCrop(newSourceRect, newDestRect, dragPercent)
            scaleTransform(newDestRect, scalePercent)
            canvas.drawBitmap(it, newSourceRect, newDestRect, newBitmapPainter)
        }
        blurBitmap?.let {
            samplingRect.set(0, 0, it.width, it.height)
            canvas.drawBitmap(it, samplingRect, destRect, bitmapPainter)
        }
        newBlurBitmap?.let {
            newSamplingRect.set(0, 0, it.width, it.height)
            canvas.drawBitmap(it, newSamplingRect, newDestRect, newBitmapPainter)
        }
    }

    /**
     * 用于执行CrossFade效果的Animator
     */
    private val animator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 300
        addListener(onStart = {
            newBitmapPainter.alpha = 0
            invalidate()
        }, onEnd = {
            newBitmapPainter.alpha = 0
            sourceBitmap = newSourceBitmap
            samplingBitmap = newSamplingBitmap
            blurBitmap = newBlurBitmap
            newSourceBitmap = null
            newSamplingBitmap = null
            newBlurBitmap = null
            invalidate()
        })
        addUpdateListener {
            val value = it.animatedValue as Float
            newBitmapPainter.alpha = (value * 255).roundToInt()
            invalidate()
        }
    }

    /**
     * 调用animator执行fadeIn过渡
     */
    private suspend fun crossFade() = withContext(Dispatchers.Main) {
        if (animator.isStarted || animator.isRunning) animator.end()
        animator.start()
    }

    /**
     * 传入模糊半径更新Blur背景
     *
     * @param radius 用于图片Blur的模糊半径参数
     */
    private suspend inline fun updateBg(radius: Int) =
        withContext(Dispatchers.IO) {
            blurBitmap = createBlurBitmap(samplingBitmap, radius)
            refresh()
        }

    /**
     * 更新Palette调色盘
     *
     * @param source 源图
     */
    private suspend inline fun updatePalette(source: Bitmap?) =
        withContext(Dispatchers.IO) {
            source ?: return@withContext
            palette.postValue(Palette.from(source).generate())
        }

    /**
     * 创建经过Blur处理的Bitmap
     *
     * @param source 源图
     * @param radius Blur所需的模糊半径参数
     * @return 经过模糊化的Bitmap
     */
    private suspend inline fun createBlurBitmap(source: Bitmap?, radius: Int): Bitmap? =
        withContext(Dispatchers.IO) {
            if (radius == 0) return@withContext null
            return@withContext stackBlur.processWithCache(source, radius)
        }

    /**
     * 重采样图片，降低图片大小，用于Blur
     *
     * @param source 源图
     * @param samplingValue 输出图片的最大边大小
     * @return 经过重采样的Bitmap
     */
    private suspend inline fun createSamplingBitmap(source: Bitmap?, samplingValue: Int): Bitmap? =
        withContext(Dispatchers.IO) {
            source ?: return@withContext source

            val width = source.width
            val height = source.height
            val matrix = Matrix()

            val scaleWidth = samplingValue.toFloat() / width
            val scaleHeight = samplingValue.toFloat() / height
            matrix.postScale(scaleWidth, scaleHeight)

            return@withContext Bitmap.createBitmap(
                source, 0, 0, width, height, matrix, false
            )
        }

    /**
     * 加载指定uri的图片
     *
     * @param uri 图片地址（可为本地亦或是网络图片）
     */
    override fun setImageURI(uri: Uri?) {
        if (uri == null) clearImage()

        load(uri) {
            if (width > 0) size(width)
            allowHardware(false)
            target(onError = {
                clearImage()
            }, onSuccess = {
                loadImageFromDrawable(it)
            })
        }
    }

    /**
     * 外部创建Coil的ImageRequest，传入onSucceed的Drawable
     */
    fun loadImageFromDrawable(drawable: Drawable) = launch(Dispatchers.IO) {
        newSourceBitmap = drawable.toBitmap().addShadow(
            Color.argb(55, 0, 0, 0),
            Color.TRANSPARENT,
            0.25f,
            GradientDrawable.Orientation.TOP_BOTTOM,
            GradientDrawable.Orientation.BOTTOM_TOP
        )
        newSamplingBitmap = createSamplingBitmap(newSourceBitmap, samplingValue)
        updatePalette(newSamplingBitmap)
        newBlurBitmap = createBlurBitmap(newSamplingBitmap, blurRadius)
        crossFade()
    }

    /**
     * 清除旧数据
     */
    fun clearImage() = launch(Dispatchers.IO) {
        this@BlurImageView.sourceBitmap = null
        this@BlurImageView.samplingBitmap = null
        this@BlurImageView.blurBitmap = null
        this@BlurImageView.newSourceBitmap = null
        this@BlurImageView.newSamplingBitmap = null
        this@BlurImageView.newBlurBitmap = null
        palette.postValue(null)
        stackBlur.evictAll()
        refresh()
    }

    private suspend inline fun refresh() =
        withContext(Dispatchers.Main) {
            invalidate()
        }
}