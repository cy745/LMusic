package com.lalilu.lmusic.adapter

import android.text.TextUtils
import androidx.databinding.BindingAdapter
import androidx.media3.common.MediaItem
import androidx.palette.graphics.Palette
import coil.load
import com.lalilu.R
import com.lalilu.common.ColorAnimator.setBgColorFromPalette
import com.lalilu.lmedia.indexer.Library
import com.lalilu.lmusic.ui.BlurImageView
import com.lalilu.ui.NewProgressBar
import com.lalilu.ui.appbar.AppbarLayout
import com.lalilu.ui.appbar.CollapsingLayout

@BindingAdapter(value = ["loadCover"], requireAll = true)
fun loadCover(imageView: BlurImageView, mediaItem: MediaItem?) {
    mediaItem ?: run {
        imageView.clearImage()
        return
    }
    val data = Library.getSongOrNull(mediaItem.mediaId) ?: run {
        imageView.clearImage()
        return
    }
    val samplingTo = imageView.width

    imageView.load(data) {
        if (samplingTo > 0) size(samplingTo)
        allowHardware(false)
        target(onSuccess = {
            imageView.loadImageFromDrawable(it)
        }, onError = {
            imageView.clearImage()
        }).build()
    }
}

@BindingAdapter("setSongTitle")
fun setSongTitle(collapsingLayout: CollapsingLayout, mediaItem: MediaItem?) {
    collapsingLayout.title =
        if (mediaItem == null || TextUtils.isEmpty(mediaItem.mediaMetadata.title)) {
            collapsingLayout.context.getString(R.string.default_slogan)
        } else {
            mediaItem.mediaMetadata.title.toString()
        }
}

@BindingAdapter("bgPalette")
fun setAppbarBGColor(appbarLayout: AppbarLayout, palette: Palette?) {
    setBgColorFromPalette(palette, appbarLayout::setBackgroundColor)
}

@BindingAdapter("bgPalette")
fun setSeekBarBGColor(seekBar: NewProgressBar, palette: Palette?) {
    setBgColorFromPalette(palette, seekBar::thumbColor::set)
}
