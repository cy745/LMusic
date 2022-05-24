package com.lalilu.lmusic.binding_adapter

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.databinding.BindingAdapter
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.palette.graphics.Palette
import coil.loadAny
import com.blankj.utilcode.util.TimeUtils
import com.lalilu.R
import com.lalilu.common.ColorAnimator.setBgColorFromPalette
import com.lalilu.lmusic.datasource.extensions.getDuration
import com.lalilu.lmusic.ui.BlurImageView
import com.lalilu.lmusic.utils.fetcher.getCoverFromMediaItem
import com.lalilu.ui.NewProgressBar
import com.lalilu.ui.appbar.AppBarLayout
import com.lalilu.ui.appbar.CollapsingLayout

@BindingAdapter("iconRec")
fun setIcon(imageView: ImageView, string: String?) {
    string ?: return
    val strings = string.split("/").toTypedArray()
    val result = when (strings[strings.size - 1].uppercase()) {
        "FLAC" -> R.drawable.ic_flac_line
        "WAV", "X-WAV" -> R.drawable.ic_wav_line
        "APE" -> R.drawable.ic_ape_line
        "MPEG", "MP3" -> R.drawable.ic_mp3_line
        else -> R.drawable.ic_mp3_line
    }
    imageView.setImageResource(result)
}

@BindingAdapter("setLyricSource")
fun setLyricSource(imageView: ImageView, mediaItem: MediaItem?) {
    mediaItem ?: return

    imageView.loadAny(mediaItem) {
        target(onSuccess = {
            imageView.visibility = View.VISIBLE
        }, onError = {
            imageView.visibility = View.INVISIBLE
        })
    }
}

@BindingAdapter(value = ["loadCover", "samplingValue"], requireAll = false)
fun loadCover(imageView: BlurImageView, mediaItem: MediaItem?, samplingValue: Int = -1) {
    mediaItem ?: run {
        imageView.clearImage()
        return
    }
    val samplingTo = if (samplingValue <= 0)
        imageView.width else samplingValue

    imageView.loadAny(mediaItem.getCoverFromMediaItem()) {
        if (samplingTo > 0) size(samplingTo)
        allowHardware(false)
        target(onSuccess = {
            imageView.loadImageFromDrawable(it)
        }, onError = {
            imageView.clearImage()
        }).build()
    }
}

@BindingAdapter(value = ["loadCover", "samplingValue"], requireAll = false)
fun loadCover(imageView: AppCompatImageView, mediaItem: MediaItem?, samplingValue: Int = -1) {
    mediaItem ?: return
    val samplingTo = if (samplingValue <= 0)
        imageView.width else samplingValue

    imageView.loadAny(mediaItem.getCoverFromMediaItem()) {
        if (samplingTo > 0) size(samplingTo)
        error(R.drawable.ic_music_line)
        crossfade(150)
    }
}

@BindingAdapter("setSongTitle")
fun setSongTitle(collapsingLayout: CollapsingLayout, mediaItem: MediaItem?) {
    collapsingLayout.title =
        if (mediaItem == null || android.text.TextUtils.isEmpty(mediaItem.mediaMetadata.title)) {
            collapsingLayout.context.getString(R.string.default_slogan)
        } else {
            mediaItem.mediaMetadata.title.toString()
        }
}

@BindingAdapter("bgPalette")
fun setAppbarBGColor(appBarLayout: AppBarLayout, palette: Palette?) {
    setBgColorFromPalette(palette, appBarLayout::setBackgroundColor)
}

@BindingAdapter("bgPalette")
fun setSeekBarBGColor(seekBar: NewProgressBar, palette: Palette?) {
    setBgColorFromPalette(palette, seekBar::thumbColor::set)
}

@BindingAdapter("setDuration")
fun setDuration(textView: TextView, duration: Long) {
    textView.text = TimeUtils.millis2String(duration, "mm:ss")
}

@BindingAdapter("setDuration")
fun setDuration(textView: TextView, metadata: MediaMetadata) {
    setDuration(textView, metadata.getDuration())
}