package com.lalilu.lmusic.binding_adapter

import android.net.Uri
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import com.lalilu.lmusic.ui.drawee.BlurImageView
import com.lalilu.lmusic.utils.EmbeddedDataUtils

fun BlurImageView.setCoverSourceUri(uri: Uri?) {
    uri ?: return

    EmbeddedDataUtils.loadCover(context, uri, onError = {
        clearImage()
    }, onSuccess = {
        loadImageFromDrawable(it)
    })
}

fun AppCompatImageView.setCoverSourceUri(uri: Uri?, samplingValue: Int = -1) {
    uri ?: return
    val samplingTo = if (samplingValue <= 0)
        width else samplingValue

    EmbeddedDataUtils.loadCover(
        context, mediaUri = uri,
        samplingValue = samplingTo,
        onStart = {
            scaleType = ImageView.ScaleType.CENTER
            setImageDrawable(it)
        }, onError = {
            scaleType = ImageView.ScaleType.CENTER
            setImageDrawable(it)
        }, onSuccess = {
            setImageDrawable(it)
            scaleType = ImageView.ScaleType.CENTER_CROP
        })
}
