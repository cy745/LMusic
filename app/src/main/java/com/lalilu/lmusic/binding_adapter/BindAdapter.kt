package com.lalilu.lmusic.binding_adapter

import android.net.Uri
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.MutableLiveData
import androidx.media3.common.MediaMetadata
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.imageLoader
import coil.load
import coil.request.ImageRequest
import com.lalilu.R
import com.lalilu.lmusic.datasource.extensions.getDuration
import com.lalilu.lmusic.datasource.extensions.getSongData
import com.lalilu.lmusic.ui.drawee.BlurImageView
import com.lalilu.lmusic.ui.seekbar.LMusicSeekBar
import com.lalilu.lmusic.utils.ColorAnimator.setBgColorFromPalette
import com.lalilu.lmusic.utils.ColorUtils.getAutomaticColor
import com.lalilu.lmusic.utils.EmbeddedDataUtils
import com.lalilu.lmusic.utils.GridItemDecoration
import com.lalilu.lmusic.utils.TextUtils
import com.lalilu.lmusic.utils.fetcher.toEmbeddedLyricSource
import com.lalilu.material.appbar.AppBarLayout


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
fun setLyricSource(imageView: ImageView, mediaMetadata: MediaMetadata) {
    val songData = mediaMetadata.getSongData()

    val imageRequest = ImageRequest.Builder(imageView.context)
        .data(songData.toEmbeddedLyricSource())
        .target(onSuccess = {
            imageView.visibility = View.VISIBLE
        }, onError = {
            imageView.visibility = View.INVISIBLE
        }).build()

    imageView.context.imageLoader.enqueue(imageRequest)
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

@BindingAdapter(value = ["setCoverSourceUri", "samplingValue"], requireAll = false)
fun setCoverSourceUri(imageView: AppCompatImageView, uri: Uri?, samplingValue: Int = -1) {
    uri ?: return
    val samplingTo = if (samplingValue <= 0)
        imageView.width else samplingValue

    EmbeddedDataUtils.loadCover(
        imageView.context, mediaUri = uri,
        samplingValue = samplingTo,
        onStart = {
            imageView.scaleType = ImageView.ScaleType.CENTER
            imageView.setImageDrawable(it)
        }, onError = {
            imageView.scaleType = ImageView.ScaleType.CENTER
            imageView.setImageDrawable(it)
        }, onSuccess = {
            imageView.setImageDrawable(it)
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        })
}

@BindingAdapter("bgPaletteLiveData")
fun setBGPaletteLiveData(
    imageView: BlurImageView, liveData: MutableLiveData<Palette?>
) {
    imageView.palette = liveData
}

@BindingAdapter("bgPalette")
fun setAppbarBGColor(appBarLayout: AppBarLayout, palette: Palette?) {
    setBgColorFromPalette(palette, appBarLayout)
}

@BindingAdapter("bgPalette")
fun setSeekBarBGColor(seekBar: LMusicSeekBar, palette: Palette?) {
    seekBar.setThumbColor(getAutomaticColor(palette))
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