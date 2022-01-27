package com.lalilu.lmusic.ui.drawee

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.net.Uri
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.animation.addListener
import androidx.lifecycle.MutableLiveData
import androidx.palette.graphics.Palette
import coil.imageLoader
import coil.request.ImageRequest
import com.commit451.nativestackblur.NativeStackBlur
import com.lalilu.lmusic.utils.toBitmap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.math.roundToInt

/**
 * 附带有Palette的ImageView
 */
class PaletteImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : AppCompatImageView(context, attrs), CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.IO
    var palette: MutableLiveData<Palette> = MutableLiveData(null)
    private var blurRadius = 0
    private var samplingValue: Int = 400

    private var bitmapPainter: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var sourceBitmap: Bitmap? = null
    private var samplingBitmap: Bitmap? = null
    private var blurBitmap: Bitmap? = null

    private var sourceRect = Rect()
    private var samplingRect = Rect()
    private var destRect = RectF()

    override fun onDraw(canvas: Canvas) {
        destRect.set(0f, 0f, width.toFloat(), height.toFloat())
        sourceBitmap?.let {
            sourceRect.set(0, 0, it.width, it.height)
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

    private suspend fun FadeIn() = withContext(Dispatchers.Main) {
        if (animator.isStarted || animator.isRunning) animator.end()
        animator.start()
    }

    fun blurBg(percent: Float) = launch(Dispatchers.IO) {
        blurRadius = (percent * 50).roundToInt()
        updateBg(blurRadius)
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
            if (source == null || radius == 0) return@withContext null
            return@withContext NativeStackBlur.process(source, radius)
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

    override fun setImageURI(uri: Uri?) {
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
                        sourceBitmap = it.toBitmap()
                        samplingBitmap = createSamplingBitmap(sourceBitmap, samplingValue)
                        updatePalette(samplingBitmap)
                        FadeIn()
                        updateBg(blurRadius)
                    }
                }.build()
        )
    }
}