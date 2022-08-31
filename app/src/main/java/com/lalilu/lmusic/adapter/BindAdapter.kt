package com.lalilu.lmusic.adapter

import androidx.databinding.BindingAdapter
import androidx.palette.graphics.Palette
import coil.load
import com.lalilu.R
import com.lalilu.common.ColorAnimator.setBgColorFromPalette
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmusic.ui.BlurImageView
import com.lalilu.ui.NewProgressBar
import com.lalilu.ui.appbar.AppbarLayout
import com.lalilu.ui.appbar.CollapsingLayout

@BindingAdapter("loadCover")
fun loadCover(imageView: BlurImageView, song: LSong?) {
    song ?: run {
        imageView.clearImage()
        return
    }
    val samplingTo = imageView.width

    imageView.load(song) {
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
fun setSongTitle(collapsingLayout: CollapsingLayout, song: LSong?) {
    collapsingLayout.title = song?.name?.takeIf { it.isNotEmpty() }
        ?: collapsingLayout.context.getString(R.string.default_slogan)
}

@BindingAdapter("bgPalette")
fun setAppbarBGColor(appbarLayout: AppbarLayout, palette: Palette?) {
    setBgColorFromPalette(palette, appbarLayout::setBackgroundColor)
}

@BindingAdapter("bgPalette")
fun setSeekBarBGColor(seekBar: NewProgressBar, palette: Palette?) {
    setBgColorFromPalette(palette, seekBar::thumbColor::set)
}
