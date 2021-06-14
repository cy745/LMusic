package com.lalilu.lmusic.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.util.AttributeSet
import androidx.lifecycle.MutableLiveData
import androidx.palette.graphics.Palette
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.imagepipeline.request.BasePostprocessor
import com.facebook.imagepipeline.request.ImageRequest
import com.facebook.imagepipeline.request.ImageRequestBuilder


class PaletteDraweeView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : SimpleDraweeView(context, attrs, defStyleAttr) {
    var palette: MutableLiveData<Palette> = MutableLiveData(null)
    var oldPalette: Palette? = null


    override fun setImageURI(uri: Uri?, callerContext: Any?) {
        super.setImageURI(uri, callerContext)
        val controllerBuilder = controllerBuilder

        controllerBuilder.setOldController(controller).callerContext = callerContext

        val imageRequestBuilder = ImageRequestBuilder.newBuilderWithSource(uri)
            .setLowestPermittedRequestLevel(ImageRequest.RequestLevel.FULL_FETCH)
            .setProgressiveRenderingEnabled(true)
            .setPostprocessor(object : BasePostprocessor() {
                override fun process(bitmap: Bitmap) {
                    oldPalette = palette.value
                    palette.postValue(Palette.from(bitmap).generate())
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
            })

        controllerBuilder.imageRequest = imageRequestBuilder.build()
        controllerBuilder.autoPlayAnimations = true
        controller = controllerBuilder.build()
    }

    private var mBackShadowDrawableLR: GradientDrawable? = null
    fun addShadow(
        bm: Bitmap,
        fromColor: Int,
        toColor: Int,
        Orientation: GradientDrawable.Orientation,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int
    ): Bitmap {
        val mBackShadowColors = intArrayOf(fromColor, toColor)
        mBackShadowDrawableLR =
            GradientDrawable(Orientation, mBackShadowColors)
        mBackShadowDrawableLR!!.gradientType = GradientDrawable.LINEAR_GRADIENT
        mBackShadowDrawableLR!!.setBounds(left, top, right, bottom)
        val canvas = Canvas(bm)
        mBackShadowDrawableLR!!.draw(canvas)
        return bm
    }
}