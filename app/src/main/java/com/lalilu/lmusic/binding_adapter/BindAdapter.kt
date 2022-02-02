package com.lalilu.lmusic.binding_adapter

import android.net.Uri
import android.text.TextUtils
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.MutableLiveData
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.imageLoader
import coil.request.ImageRequest
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.lalilu.R
import com.lalilu.lmusic.adapter.PlayingAdapter
import com.lalilu.lmusic.domain.entity.MSong
import com.lalilu.lmusic.toEmbeddedCoverSource
import com.lalilu.lmusic.toEmbeddedLyricSource
import com.lalilu.lmusic.ui.drawee.BlurImageView
import com.lalilu.lmusic.ui.seekbar.LMusicSeekBar
import com.lalilu.lmusic.utils.ColorAnimator.setBgColorFromPalette
import com.lalilu.lmusic.utils.ColorUtils.getAutomaticColor
import com.lalilu.lmusic.utils.GridItemDecoration


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
fun setHasLyricIcon(imageView: ImageView, songData: String?) {
    songData ?: return

    val imageRequest = ImageRequest.Builder(imageView.context)
        .data(songData.toEmbeddedLyricSource())
        .target(imageView)
        .build()

    imageView.context.imageLoader.enqueue(imageRequest)
}

@BindingAdapter(value = ["pictureUri", "samplingValue"], requireAll = false)
fun setPictureUri(imageView: AppCompatImageView, uri: Uri?, samplingValue: Int = -1) {
    uri ?: return

    val imageRequest = ImageRequest.Builder(imageView.context)
        .data(uri.toEmbeddedCoverSource())
        .allowHardware(false)
        .placeholder(R.drawable.ic_loader_line)
        .error(R.drawable.ic_error_warning_line)
        .target(onStart = {
            imageView.scaleType = ImageView.ScaleType.CENTER
        }, onError = {
            imageView.scaleType = ImageView.ScaleType.CENTER
        }, onSuccess = {
            imageView.setImageDrawable(it)
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        })

    if (samplingValue > 0) imageRequest.size(samplingValue)
    imageView.context.imageLoader.enqueue(imageRequest.build())
}

@BindingAdapter(value = ["artUri"])
fun setMSongCoverUri(imageView: BlurImageView, uri: Uri?) {
    uri ?: return

    imageView.context.imageLoader.enqueue(
        ImageRequest.Builder(imageView.context)
            .data(uri.toEmbeddedCoverSource())
            .allowHardware(false)
            .target(onSuccess = {
                imageView.loadImageFromDrawable(it)
            }, onError = {
                imageView.clearImage()
            }).build()
    )
}

@BindingAdapter(value = ["bindTitle"], requireAll = false)
fun setMSongTitle(collapsingToolbarLayout: CollapsingToolbarLayout, title: String?) {
    collapsingToolbarLayout.title = if (TextUtils.isEmpty(title)) "LMusic..." else title
}

@BindingAdapter(value = ["bgPaletteLiveData"])
fun setBGPaletteLiveData(
    imageView: BlurImageView, liveData: MutableLiveData<Palette?>
) {
    imageView.palette = liveData
}

@BindingAdapter(value = ["bgPalette"], requireAll = false)
fun setAppbarBGColor(appBarLayout: AppBarLayout, palette: Palette?) {
    setBgColorFromPalette(palette, appBarLayout)
}

@BindingAdapter(value = ["bgPalette"], requireAll = false)
fun setSeekBarBGColor(seekBar: LMusicSeekBar, palette: Palette?) {
    seekBar.setThumbColor(getAutomaticColor(palette))
}

@BindingAdapter(value = ["gridGap", "gridSpanCount"], requireAll = true)
fun addGridItemDecoration(recyclerView: RecyclerView, gridGap: Int, gridSpanCount: Int) {
    recyclerView.layoutManager = GridLayoutManager(recyclerView.context, gridSpanCount)
    recyclerView.addItemDecoration(GridItemDecoration(gridGap, gridSpanCount))
}

fun PlayingAdapter.setItems(
    list: List<MSong>?,
    recyclerView: RecyclerView
) {
    val newList = list?.toMutableList() ?: ArrayList()
    var oldList = this.data.toMutableList()

    // 预先将头部部分差异进行转移
    val size = oldList.indexOfFirst { it.songId == newList[0].songId }
    if (size > 0 && size >= oldList.size / 2 &&
        recyclerView.computeVerticalScrollOffset() >
        recyclerView.computeVerticalScrollRange() / 2
    ) {
        val temp = oldList.take(size).toMutableList()
        temp.addAll(0, oldList.drop(size))
        oldList = temp

        this.notifyItemRangeRemoved(0, size)
        this.notifyItemRangeInserted(oldList.size, size)
    }
    val diffCallback = MSong.DiffMSong(oldList, newList)
    val diffResult = DiffUtil.calculateDiff(diffCallback, false)
    this.data = newList
    diffResult.dispatchUpdatesTo(this)
    recyclerView.scrollToPosition(0)
}