package com.lalilu.lmusic.ui.drawee

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.imagepipeline.common.ResizeOptions
import com.facebook.imagepipeline.request.ImageRequestBuilder

/**
 * 默认提供重采样大小功能的 DraweeView
 */
open class SamplingDraweeView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : SimpleDraweeView(context, attrs, defStyleAttr) {
    open var samplingValue = 200

    override fun setImageURI(uri: Uri?, callerContext: Any?) {
        if (uri == null) return

        val imageRequestBuilder = ImageRequestBuilder.newBuilderWithSource(uri)
            .setResizeOptions(ResizeOptions(samplingValue, samplingValue))

        controllerBuilder.imageRequest = imageRequestBuilder.build()
        controllerBuilder.autoPlayAnimations = true
        controller = controllerBuilder.build()
    }
}