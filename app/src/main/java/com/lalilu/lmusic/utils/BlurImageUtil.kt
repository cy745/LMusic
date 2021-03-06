package com.lalilu.lmusic.utils

import android.animation.ValueAnimator
import android.graphics.*
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.annotation.FloatRange
import androidx.core.animation.addListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.math.abs
import kotlin.math.roundToInt

object BlurImageUtil : CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.IO
    const val CROSS_FADE_DURATION = 300L
    const val MAX_BLUR_RADIUS = 50

    private val dInterpolator = DecelerateInterpolator()
    private val aInterpolator = AccelerateInterpolator()

    class BlurImageLayer(
        private val sourceBitmap: Bitmap,
        blurRadius: Int = 0,
        private val samplingValue: Int = 400,
        onCreate: suspend (Bitmap) -> Unit = {}
    ) {
        private lateinit var samplingBitmap: Bitmap
        private var blurBitmap: Bitmap? = null
        private var bitmapPainter: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
            .apply { alpha = 0 }
        private var sourceRect = Rect()
        private var destRectF = RectF()
        private var blurRect = Rect()

        @FloatRange(from = 0.0, to = 1.0)
        var alpha: Float = 0f
            set(value) {
                field = value
                bitmapPainter.alpha = (value * 255).roundToInt()
            }

        var radius: Int = blurRadius
            set(value) {
                field = value.coerceIn(0, 50)
                safeLaunch(Dispatchers.IO) {
                    blurBitmap = createBlurBitmap(samplingBitmap, value)
                }
            }

        fun drawSourceImage(
            canvas: Canvas,
            preProcessing: (Rect, RectF) -> Unit = { _, _ -> }
        ) {
            sourceBitmap.takeIf { !it.isRecycled }?.let {
                sourceRect.set(0, 0, it.width, it.height)
                preProcessing(sourceRect, destRectF)
                canvas.drawBitmap(it, sourceRect, destRectF, bitmapPainter)
            }
        }

        fun drawBlurImage(canvas: Canvas) {
            blurBitmap?.takeIf { !it.isRecycled }?.let {
                blurRect.set(0, 0, it.width, it.height)
                canvas.drawBitmap(it, blurRect, destRectF, bitmapPainter)
            }
        }

        fun recycle() {
            sourceBitmap.recycle()
            samplingBitmap.recycle()
            blurBitmap?.recycle()
        }

        init {
            safeLaunch {
                samplingBitmap = createSamplingBitmap(sourceBitmap, samplingValue)
                blurBitmap = createBlurBitmap(samplingBitmap, radius)
                onCreate(samplingBitmap)
            }
        }
    }

    fun View.updateBlur(layer: BlurImageLayer, radius: Int) = safeLaunch {
        layer.radius = radius
        withContext(Dispatchers.Main) {
            invalidate()
        }
    }

    fun View.crossFade(layer: BlurImageLayer?, onEnd: () -> Unit = {}) =
        safeLaunch(Dispatchers.Main) {
            ValueAnimator.ofFloat(0f, 1f).apply {
                duration = CROSS_FADE_DURATION
                addListener(onStart = {
                    layer?.alpha = 0f
                    invalidate()
                }, onEnd = { onEnd() })
                addUpdateListener { animator ->
                    val value = animator.animatedValue as Float
                    layer?.alpha = value
                    invalidate()
                }
            }.start()
        }

    /**
     * ??????CenterCrop??????????????????????????????dest??????
     *
     * @param source Bitmap?????????
     * @param dest ????????????
     */
    fun centerCrop(
        source: Rect,
        dest: RectF,
        offsetPercent: Float
    ): Int {
        val w = source.width().toFloat()
        val h = source.height().toFloat()
        val x = dest.width()
        val y = dest.height()

        val sourceWHRatio = w / h
        val offset: Float

        var maxOffset = 0
        if (sourceWHRatio > x / y) {
            // ?????????????????????
            offset = abs(x - sourceWHRatio * y) / 2
            dest.set(
                dest.left - offset, dest.top,
                x + offset, y
            )
        } else {
            // ?????????????????????
            maxOffset = abs(y - x / sourceWHRatio).toInt()
            offset = maxOffset / 2f
            dest.set(
                dest.left, dest.top - offset * (1 - offsetPercent),
                x, y + offset * (1 + offsetPercent)
            )
        }
        return maxOffset
    }

    /**
     * ???????????????????????????ImageView?????????????????????View??????????????????
     */
    fun View.scaleTransform(dest: RectF, scalePercent: Float) {
        val dValue = dInterpolator.getInterpolation(scalePercent)
        val aValue = aInterpolator.getInterpolation(scalePercent)
        val offsetValue = (height.toFloat() - dest.height()) / 2f * dValue
        val scaleValue = 1 + (height.toFloat() / dest.height() - 1) * aValue

        dest.top += offsetValue
        dest.bottom += offsetValue
        scaleX = scaleValue
        scaleY = scaleValue
    }

    /**
     * ????????????Blur?????????Bitmap
     *
     * @param source ??????
     * @param radius Blur???????????????????????????
     * @return ??????????????????Bitmap
     */
    suspend inline fun createBlurBitmap(
        source: Bitmap?,
        radius: Int,
    ): Bitmap? =
        withContext(Dispatchers.IO) {
            if (radius == 0 || source == null) return@withContext null
            return@withContext StackBlurUtils.processWithCache(source, radius)
        }

    /**
     * ?????????????????????????????????????????????Blur
     *
     * @param source ??????
     * @param samplingValue ??????????????????????????????
     * @return ??????????????????Bitmap
     */
    suspend inline fun createSamplingBitmap(source: Bitmap, samplingValue: Int): Bitmap =
        withContext(Dispatchers.IO) {
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
}