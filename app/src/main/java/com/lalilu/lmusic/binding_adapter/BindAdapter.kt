package com.lalilu.lmusic.binding_adapter

import android.net.Uri
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.MutableLiveData
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.imageLoader
import coil.load
import coil.request.ImageRequest
import com.dirror.lyricviewx.LyricViewX
import com.lalilu.R
import com.lalilu.lmusic.adapter.PlayingAdapter
import com.lalilu.lmusic.domain.entity.MSong
import com.lalilu.lmusic.toEmbeddedLyricSource
import com.lalilu.lmusic.ui.drawee.BlurImageView
import com.lalilu.lmusic.ui.seekbar.LMusicSeekBar
import com.lalilu.lmusic.utils.ColorAnimator.setBgColorFromPalette
import com.lalilu.lmusic.utils.ColorUtils.getAutomaticColor
import com.lalilu.lmusic.utils.EmbeddedDataUtils
import com.lalilu.lmusic.utils.GridItemDecoration
import com.lalilu.lmusic.utils.moveHeadToTail
import com.lalilu.material.appbar.AppBarLayout
import com.lalilu.material.appbar.CollapsingToolbarLayout


@BindingAdapter("iconRec")
fun setIcon(imageView: ImageView, string: String) {
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

@BindingAdapter("setLyricSourceUri")
fun setLyricSourceUri(imageView: ImageView, songData: String?) {
    songData ?: return

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

    imageView.load(uri) {
        if (samplingValue > 0) size(samplingValue)
        crossfade(300)
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

@BindingAdapter("setCoverSourceUri")
fun setCoverSourceUri(imageView: BlurImageView, uri: Uri?) {
    uri ?: return

    EmbeddedDataUtils.loadCover(imageView.context, uri, onError = {
        imageView.clearImage()
    }, onSuccess = {
        imageView.loadImageFromDrawable(it)
    })
}

@BindingAdapter("bindTitle")
fun setMSongTitle(collapsingToolbarLayout: CollapsingToolbarLayout, title: String?) {
    collapsingToolbarLayout.title = if (TextUtils.isEmpty(title)) "LMusic..." else title
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

@BindingAdapter("loadLyric")
fun loadLyric(lyricViewX: LyricViewX, lyric: String?) {
    lyricViewX.loadLyric(lyric)
}

@BindingAdapter("updateTime")
fun updateTime(lyricViewX: LyricViewX, time: Long) {
    lyricViewX.updateTime(time)
}

@BindingAdapter("setSongs")
fun setSongs(recyclerView: RecyclerView, songs: List<MSong>?) {
    val adapter = recyclerView.adapter as PlayingAdapter

    val newList = songs?.toMutableList() ?: ArrayList()
    var oldList = adapter.data.toMutableList()

    // 预先将头部部分差异进行转移
    val size = oldList.indexOfFirst { it.songId == newList[0].songId }
    if (size > 0 && size >= oldList.size / 2 &&
        recyclerView.computeVerticalScrollOffset() >
        recyclerView.computeVerticalScrollRange() / 2
    ) {
        oldList = oldList.moveHeadToTail(size)

        adapter.notifyItemRangeRemoved(0, size)
        adapter.notifyItemRangeInserted(oldList.size, size)
    }
    val diffCallback = MSong.DiffMSong(oldList, newList)
    val diffResult = DiffUtil.calculateDiff(diffCallback, false)
    adapter.data = newList
    diffResult.dispatchUpdatesTo(adapter)
    recyclerView.scrollToPosition(0)
}