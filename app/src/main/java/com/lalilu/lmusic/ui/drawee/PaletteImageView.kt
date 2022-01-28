package com.lalilu.lmusic.ui.drawee

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.net.Uri
import android.util.AttributeSet
import android.view.View
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

    @Inject
    lateinit var stackBlur: StackBlurUtils

    override val coroutineContext: CoroutineContext = Dispatchers.IO
    var palette: MutableLiveData<Palette> = MutableLiveData(null)
    var maxOffset = 0

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
        set(value) {
            field = value
            launch(Dispatchers.IO) {
                updateBg(blurRadius)
            }
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
     * 计算图片可拖动展开的最大位移距离
     */
    private fun calcMaxOffset(source: Bitmap?): Int {
        source ?: return 0

        return if (source.width / source.height > 0) 0
        else (width * source.height / source.width - width)
            .coerceAtLeast(0)
    }

    /**
     * 按照CenterCrop的规则，获取调整后的dest位置
     *
     * @param source Bitmap的大小
     * @param dest 目标位置
     */
    private fun centerCrop(
        source: Rect,
        dest: RectF,
        offsetPercent: Float,
        scalePercent: Float
    ): RectF {
        val w = source.width().toFloat()
        val h = source.height().toFloat()
        val x = dest.width()
        val y = dest.height()

        val sourceWHRatio = w / h
        val offset: Float

        if (sourceWHRatio > x / y) {
            offset = abs(x - sourceWHRatio * y) / 2
            dest.set(
                dest.left - offset * (1 - offsetPercent), dest.top,
                x + offset * (1 + offsetPercent), y
            )
        } else {
            offset = abs(y - x / sourceWHRatio) / 2
            dest.set(
                dest.left, dest.top - offset * (1 - offsetPercent),
                x, y + offset * (1 + offsetPercent)
            )
        }
        return dest
    }

    override fun onDraw(canvas: Canvas) {
        destRect.set(0f, 0f, width.toFloat(), width.toFloat())
        sourceBitmap?.let {
            sourceRect.set(0, 0, it.width, it.height)
            centerCrop(sourceRect, destRect, dragPercent, scalePercent)
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

    private suspend fun fadeIn() = withContext(Dispatchers.Main) {
        if (animator.isStarted || animator.isRunning) animator.end()
        animator.start()
    }

    private suspend inline fun updateBg(radius: Int) =
        withContext(Dispatchers.IO) {
            blurBitmap = createBlurBitmap(samplingBitmap, radius)
            withContext(Dispatchers.Main) {
                invalidate()
            }
        }

    private suspend inline fun updatePalette(source: Bitmap?) =
        withContext(Dispatchers.IO) {
            source ?: return@withContext
            palette.postValue(Palette.from(source).generate())
        }

    private suspend inline fun createBlurBitmap(source: Bitmap?, radius: Int): Bitmap? =
        withContext(Dispatchers.IO) {
            if (radius == 0) return@withContext null
            return@withContext stackBlur.processWithCache(source, radius)
        }

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
                        maxOffset = calcMaxOffset(sourceBitmap)
                        samplingBitmap = createSamplingBitmap(sourceBitmap, samplingValue)
                        updatePalette(samplingBitmap)
                        updateBg(blurRadius)
                    }
                }.build()
        )
    }
}