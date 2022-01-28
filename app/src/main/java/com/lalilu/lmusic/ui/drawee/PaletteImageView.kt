package com.lalilu.lmusic.ui.drawee

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.net.Uri
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import androidx.core.animation.addListener
import androidx.lifecycle.MutableLiveData
import androidx.palette.graphics.Palette
import coil.imageLoader
import coil.request.ImageRequest
import com.lalilu.lmusic.utils.StackBlurUtils
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
 * 附带有Palette的ImageView
 */
@AndroidEntryPoint
class PaletteImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs), CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.IO

    @Inject
    lateinit var stackBlur: StackBlurUtils

    var palette: MutableLiveData<Palette> = MutableLiveData(null)
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
        sourceBitmap?.let {
            sourceRect.set(0, 0, it.width, it.height)
            centerCrop(sourceRect, destRect, dragPercent)
            scaleTransform(destRect, scalePercent)
            canvas.drawBitmap(it, sourceRect, destRect, bitmapPainter)
        }
        blurBitmap?.let {
            samplingRect.set(0, 0, it.width, it.height)
            canvas.drawBitmap(it, samplingRect, destRect, bitmapPainter)
        }
    }

    private val animator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 500
        addListener(onStart = {
            bitmapPainter.alpha = 0
            invalidate()
        }, onEnd = {
            bitmapPainter.alpha = 255
            invalidate()
        })
        addUpdateListener {
            val value = it.animatedValue as Float
            bitmapPainter.alpha = (value * 255).roundToInt()
            invalidate()
        }
    }

    /**
     * 调用animator执行fadeIn过渡
     */
    private suspend fun fadeIn() = withContext(Dispatchers.Main) {
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
            withContext(Dispatchers.Main) {
                invalidate()
            }
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
     * 创建Blur的Bitmap
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
     *
     */
    fun setImageURI(uri: Uri?) {
        if (uri == null) {
            this.sourceBitmap = null
            this.samplingBitmap = null
            this.blurBitmap = null
            invalidate()
            return
        }

        context.imageLoader.enqueue(
            ImageRequest.Builder(context)
                .data(uri)
                .allowHardware(false)
                .target {
                    launch(Dispatchers.IO) {
                        fadeIn()
                        sourceBitmap = it.toBitmap()
                        samplingBitmap = createSamplingBitmap(sourceBitmap, samplingValue)
                        updatePalette(samplingBitmap)
                        updateBg(blurRadius)
                    }
                }.build()
        )
    }
}