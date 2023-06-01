package com.lalilu.lmusic.adapter

import android.graphics.Outline
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.databinding.BindingAdapter
import androidx.palette.graphics.Palette
import coil.load
import coil.util.CoilUtils.dispose
import com.blankj.utilcode.util.SizeUtils
import com.lalilu.R
import com.lalilu.common.ColorAnimator.setBgColorFromPalette
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmusic.ui.BlurImageView
import com.lalilu.lmusic.utils.extension.durationToTime
import com.lalilu.lmusic.utils.extension.getMimeTypeIconRes
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

@BindingAdapter("loadCoverForPlaying")
fun loadCoverForPlaying(imageView: AppCompatImageView, song: LSong?) {
    song ?: run {
        imageView.setImageDrawable(null)
        return
    }
    val samplingTo = imageView.width

    dispose(imageView)
    imageView.load(song) {
        if (samplingTo > 0) size(samplingTo)
        placeholder(R.drawable.ic_music_line_bg_64dp)
        error(R.drawable.ic_music_line_bg_64dp)
    }
}

@BindingAdapter("setRoundOutline")
fun setRoundOutline(imageView: AppCompatImageView, radius: Number) {
    imageView.outlineProvider = object : ViewOutlineProvider() {
        override fun getOutline(view: View, outline: Outline) {
            outline.setRoundRect(
                0, 0, view.width, view.height,
                SizeUtils.dp2px(radius.toFloat()).toFloat()
            )
        }
    }
    imageView.clipToOutline = true
}

@BindingAdapter("iconRec")
fun iconRec(imageView: ImageView, mimeType: String) {
    imageView.setImageResource(getMimeTypeIconRes(mimeType))
}

@BindingAdapter("setDuration")
fun setDuration(textView: AppCompatTextView, duration: Long) {
    textView.text = duration.durationToTime()
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
