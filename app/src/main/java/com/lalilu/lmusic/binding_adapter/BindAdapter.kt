package com.lalilu.lmusic.binding_adapter

import android.net.Uri
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.MutableLiveData
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.loadAny
import com.lalilu.R
import com.lalilu.lmusic.datasource.extensions.getDuration
import com.lalilu.lmusic.ui.drawee.BlurImageView
import com.lalilu.ui.NewProgressBar
import com.lalilu.common.ColorAnimator.setBgColorFromPalette
import com.lalilu.common.ColorUtils.getAutomaticColor
import com.lalilu.lmusic.utils.GridItemDecoration
import com.lalilu.common.TextUtils
import com.lalilu.lmusic.utils.fetcher.getCoverFromMediaItem
import com.lalilu.material.appbar.AppBarLayout
import com.lalilu.material.appbar.CollapsingToolbarLayout

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

@BindingAdapter(value = ["setNormalUri", "samplingValue"], requireAll = false)
fun setNormalUri(imageView: AppCompatImageView, uri: Uri?, samplingValue: Int = -1) {
    uri ?: return
    val samplingTo = if (samplingValue <= 0)
        imageView.width else samplingValue

    imageView.load(uri) {
        if (samplingValue > 0) size(samplingTo)
        crossfade(150)
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
        crossfade(150)
    }
}

@BindingAdapter("setSongTitle")
fun setSongTitle(collapsingToolbarLayout: CollapsingToolbarLayout, mediaItem: MediaItem?) {
    collapsingToolbarLayout.title =
        if (mediaItem == null || android.text.TextUtils.isEmpty(mediaItem.mediaMetadata.title)) {
            collapsingToolbarLayout.context.getString(R.string.default_slogan)
        } else {
            mediaItem.mediaMetadata.title.toString()
        }
}

@BindingAdapter("bgPaletteLiveData")
fun setBGPaletteLiveData(
    imageView: BlurImageView,
    liveData: MutableLiveData<Palette?>
) {
    imageView.palette = liveData
}

@BindingAdapter("bgPalette")
fun setAppbarBGColor(appBarLayout: AppBarLayout, palette: Palette?) {
    setBgColorFromPalette(palette, appBarLayout)
}

@BindingAdapter("bgPalette")
fun setSeekBarBGColor(seekBar: NewProgressBar, palette: Palette?) {
    seekBar.thumbColor = getAutomaticColor(palette)
}

@BindingAdapter(value = ["gridGap", "gridSpanCount"], requireAll = true)
fun addGridItemDecoration(recyclerView: RecyclerView, gridGap: Int, gridSpanCount: Int) {
    recyclerView.layoutManager = GridLayoutManager(recyclerView.context, gridSpanCount)
    recyclerView.addItemDecoration(GridItemDecoration(gridGap, gridSpanCount))
}

@BindingAdapter("setDuration")
fun setDuration(textView: TextView, metadata: MediaMetadata) {
    textView.text = TextUtils.durationToString(metadata.getDuration())
}

@BindingAdapter("setDuration")
fun setDuration(textView: TextView, duration: Long) {
    textView.text = TextUtils.durationToString(duration)
}