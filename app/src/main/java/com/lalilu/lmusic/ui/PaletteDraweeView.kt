package com.lalilu.lmusic.ui

import android.content.Context
import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.util.AttributeSet
import androidx.lifecycle.MutableLiveData
import androidx.palette.graphics.Palette
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.imagepipeline.request.BaseRepeatedPostProcessor
import com.facebook.imagepipeline.request.ImageRequest
import com.facebook.imagepipeline.request.ImageRequestBuilder
import com.lalilu.lmusic.utils.FastBlur

/**
 * 包含提取图片主题色功能的 DraweeView
 *
 */
class PaletteDraweeView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : SimpleDraweeView(context, attrs, defStyleAttr) {
    var palette: MutableLiveData<Palette> = MutableLiveData(null)
    var blurRadius = 0

    var samplingValue = 400
    var samplingBitmap: Bitmap? = null
    var samplingRect = Rect(0, 0, samplingValue, samplingValue)

    fun blurBg(percent: Float) {
        blurRadius = (percent * 50).toInt()
        postProcessor.update()
    }

    /**
     *  实现 blur 动态的后处理器
     */
    private var postProcessor: BaseRepeatedPostProcessor = object : BaseRepeatedPostProcessor() {
        override fun process(bitmap: Bitmap) {
            palette.postValue(Palette.from(bitmap).generate())

            // 创建重采样bitmap
            if (samplingBitmap == null) {
                val width = bitmap.width
                val height = bitmap.height
                val matrix = Matrix()

                val scaleWidth = samplingValue.toFloat() / width
                val scaleHeight = samplingValue.toFloat() / height
                matrix.postScale(scaleWidth, scaleHeight)

                samplingBitmap = Bitmap.createBitmap(
                    bitmap, 0, 0,
                    width, height,
                    matrix, false
                )
            }

            // 根据模糊半径绘制blur后的bitmap
            if (samplingBitmap != null && blurRadius != 0) {
                val bm = FastBlur.doBlur(samplingBitmap, blurRadius, false)
                Canvas(bitmap).drawBitmap(
                    bm, samplingRect,
                    RectF(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat()),
                    null
                )
            }

            // 为图片添加上下的内阴影
            addShadow(
                bitmap, Color.argb(55, 0, 0, 0),
                Color.TRANSPARENT, GradientDrawable.Orientation.TOP_BOTTOM,
                0, 0, bitmap.width, bitmap.height / 4
            )
            addShadow(
                bitmap, Color.argb(55, 0, 0, 0),
                Color.TRANSPARENT, GradientDrawable.Orientation.BOTTOM_TOP,
                0, (bitmap.height * 0.75).toInt(), bitmap.width, bitmap.height
            )
            super.process(bitmap)
        }
    }

    override fun setImageURI(uri: Uri?, callerContext: Any?) {
        super.setImageURI(uri, callerContext)

        // 回收上一张重采样图片
        if (samplingBitmap != null) {
            samplingBitmap!!.recycle()
            samplingBitmap = null
        }

        val controllerBuilder = controllerBuilder
        controllerBuilder.setOldController(controller).callerContext = callerContext

        val imageRequestBuilder = ImageRequestBuilder.newBuilderWithSource(uri)
            .setLowestPermittedRequestLevel(ImageRequest.RequestLevel.FULL_FETCH)
            .setProgressiveRenderingEnabled(true)
            .setPostprocessor(postProcessor)

        controllerBuilder.imageRequest = imageRequestBuilder.build()
        controllerBuilder.autoPlayAnimations = true
        controller = controllerBuilder.build()
    }


    private var mBackShadowDrawableLR: GradientDrawable? = null

    /**
     * 为 bitmap 添加内阴影的方法
     */
    private fun addShadow(
        bm: Bitmap, fromColor: Int, toColor: Int,
        Orientation: GradientDrawable.Orientation,
        left: Int, top: Int, right: Int, bottom: Int
    ): Bitmap {
        val mBackShadowColors = intArrayOf(fromColor, toColor)
        mBackShadowDrawableLR = GradientDrawable(Orientation, mBackShadowColors)
        mBackShadowDrawableLR!!.gradientType = GradientDrawable.LINEAR_GRADIENT
        mBackShadowDrawableLR!!.setBounds(left, top, right, bottom)
        mBackShadowDrawableLR!!.draw(Canvas(bm))
        return bm
    }
}