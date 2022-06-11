package com.lalilu.lmusic.ui

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.AttributeSet
import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import androidx.appcompat.widget.AppCompatImageView
import androidx.lifecycle.MutableLiveData
import androidx.palette.graphics.Palette
import coil.load
import com.lalilu.lmusic.utils.BlurImageUtil
import com.lalilu.lmusic.utils.BlurImageUtil.MAX_BLUR_RADIUS
import com.lalilu.lmusic.utils.BlurImageUtil.centerCrop
import com.lalilu.lmusic.utils.BlurImageUtil.crossFade
import com.lalilu.lmusic.utils.BlurImageUtil.scaleTransform
import com.lalilu.lmusic.utils.BlurImageUtil.updateBlur
import com.lalilu.lmusic.utils.StackBlurUtils
import com.lalilu.lmusic.utils.addShadow
import com.lalilu.lmusic.utils.toBitmap
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.internal.synchronized
import kotlin.coroutines.CoroutineContext
import kotlin.math.roundToInt

/**
 * 结合StackBlur的自定义ImageView
 */
@OptIn(ExperimentalCoroutinesApi::class, InternalCoroutinesApi::class)
class BlurImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : AppCompatImageView(context, attrs), CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.IO

    var maxOffset = 0
    private val imageLayer = ArrayList<BlurImageUtil.BlurImageLayer>()
    private val drawableToDraw = MutableStateFlow<Drawable?>(null)
    private val tempDstRect = RectF()
    private var maskPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).also {
        it.color = Color.BLACK
    }
    var palette = MutableLiveData<Palette?>(null)

    @IntRange(from = 0, to = 50)
    var blurRadius = 0
        set(value) {
            field = value
            imageLayer.forEach { updateBlur(it, value) }
        }

    @FloatRange(from = 0.0, to = 1.0)
    var blurPercent: Float = 0f
        internal set(value) {
            field = value
            blurRadius = (value * MAX_BLUR_RADIUS).roundToInt()
        }

    @FloatRange(from = 0.0, to = 1.0)
    var dragPercent: Float = 0f
        set(value) {
            field = value
            invalidate()
        }

    @FloatRange(from = 0.0, to = 1.0)
    var scalePercent: Float = 0f
        set(value) {
            field = value
            invalidate()
        }

    override fun onDraw(canvas: Canvas) {
        tempDstRect.set(0f, 0f, width.toFloat(), height.toFloat())

        if (alpha >= 1f) {
            imageLayer.forEach { it.drawLayerSource(canvas) }
        } else {
            imageLayer.lastOrNull()?.drawLayerSource(canvas)
        }

        imageLayer.forEach { it.drawBlurImage(canvas) }

        maskPaint.alpha = (blurPercent * 100f).coerceIn(0f, 255f).toInt()
        canvas.drawRect(tempDstRect, maskPaint)
    }

    private fun BlurImageUtil.BlurImageLayer.drawLayerSource(canvas: Canvas) {
        drawSourceImage(canvas) { source, dest ->
            dest.set(0f, 0f, width.toFloat(), width.toFloat())
            centerCrop(source, dest, dragPercent)
            scaleTransform(dest, scalePercent)
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
        drawableToDraw.emit(drawable)
    }

    init {
        drawableToDraw.mapLatest {
            it?.toBitmap()
        }.flatMapLatest { sourceBitmap ->
            if (sourceBitmap == null) clearImage()

            flow {
                sourceBitmap?.addShadow()?.let {
                    emit(it)
                }
            }
        }.mapLatest {
            BlurImageUtil.BlurImageLayer(it, blurRadius) { samplingBitmap ->
                updatePalette(samplingBitmap)
            }
        }.onEach { layer ->
            imageLayer.add(layer)
            crossFade(layer) {
                synchronized(imageLayer) {
                    if (imageLayer.size >= 2) {
                        imageLayer.removeFirst()
                    }
                }
            }
            System.gc()
        }.launchIn(this)
    }

    /**
     * 清除旧数据
     */
    fun clearImage() = launch(Dispatchers.IO) {
        imageLayer.forEach { it.recycle() }
        imageLayer.clear()
        palette.postValue(null)
        StackBlurUtils.evictAll()
        invalidate()
        System.gc()
    }
}