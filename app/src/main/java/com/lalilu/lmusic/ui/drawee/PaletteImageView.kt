package com.lalilu.lmusic.ui.drawee

import android.content.Context
import android.graphics.*
import android.net.Uri
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.lifecycle.MutableLiveData
import androidx.palette.graphics.Palette
import coil.clear
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

class PaletteImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : AppCompatImageView(context, attrs), CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.IO
    var palette: MutableLiveData<Palette> = MutableLiveData(null)
    private var blurRadius = 0
    private var samplingValue: Int = 400

    private var sourceBitmap: Bitmap? = null
    private var blurBitmap: Bitmap? = null
    private var samplingBitmap: Bitmap? = null
    private var sourceRect = Rect()
    private var samplingRect = Rect()
    private var destRect = RectF()

    fun blurBg(percent: Float) = launch(Dispatchers.IO) {
        blurRadius = (percent * 50).roundToInt()
        updateBg(blurRadius)
    }

    override fun onDraw(canvas: Canvas) {
        destRect.set(0f, 0f, width.toFloat(), height.toFloat())
        sourceBitmap?.let {
            sourceRect.set(0, 0, it.width, it.height)
            canvas.drawBitmap(it, sourceRect, destRect, null)
        }
        blurBitmap?.let {
            samplingRect.set(0, 0, it.width, it.height)
            canvas.drawBitmap(it, samplingRect, destRect, null)
        }
    }

    private suspend fun updateBg(radius: Int) =
        withContext(Dispatchers.IO) {
            blurBitmap = if (radius == 0 || samplingBitmap == null) null
            else NativeStackBlur.process(samplingBitmap, radius)

            withContext(Dispatchers.Main) {
                invalidate()
            }
        }

    override fun setImageURI(uri: Uri?) {
        if (uri == null) {
            launch {
                samplingBitmap = null
                updateBg(blurRadius)
                this@PaletteImageView.clear()
            }
            return
        }

        context.imageLoader.enqueue(
            ImageRequest.Builder(context)
                .data(uri)
                .target {
                    launch(Dispatchers.IO) {
                        sourceBitmap = it.toBitmap()

                        val width = sourceBitmap!!.width
                        val height = sourceBitmap!!.height
                        val matrix = Matrix()

                        val scaleWidth = samplingValue.toFloat() / width
                        val scaleHeight = samplingValue.toFloat() / height
                        matrix.postScale(scaleWidth, scaleHeight)

                        samplingBitmap = Bitmap.createBitmap(
                            sourceBitmap!!, 0, 0, width, height, matrix, false
                        )
                        updateBg(blurRadius)
                    }
                }.build()
        )
//        this.load(uri) {
//            crossfade(500)
//        }
    }
}