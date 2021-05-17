package com.lalilu.lmusic

import android.content.Context
import android.graphics.Bitmap
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
                    super.process(bitmap)
                }
            })

        controllerBuilder.imageRequest = imageRequestBuilder.build()
        controllerBuilder.autoPlayAnimations = true
        controller = controllerBuilder.build()
    }
}