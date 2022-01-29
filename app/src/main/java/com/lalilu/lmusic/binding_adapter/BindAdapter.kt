package com.lalilu.lmusic.binding_adapter

import android.net.Uri
import android.text.TextUtils
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.MutableLiveData
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.lalilu.R
import com.lalilu.lmusic.EmbeddedFetcher
import com.lalilu.lmusic.adapter.MSongPlayingAdapter
import com.lalilu.lmusic.domain.entity.MSong
import com.lalilu.lmusic.ui.drawee.PaletteImageView
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

@BindingAdapter(value = ["pictureUri", "samplingValue"], requireAll = false)
fun setPictureUri(imageView: AppCompatImageView, uri: Uri?, samplingValue: Int = -1) {
    imageView.load(uri) {
        placeholder(R.drawable.ic_loader_line)
        error(R.drawable.ic_error_warning_line)
        fetcher(EmbeddedFetcher())
        if (samplingValue > 0) size(samplingValue)
    }
}

@BindingAdapter(value = ["artUri"])
fun setMSongCoverUri(imageView: PaletteImageView, uri: Uri?) {
    imageView.setImageURI(uri)
}

@BindingAdapter(value = ["bindTitle"], requireAll = false)
fun setMSongTitle(collapsingToolbarLayout: CollapsingToolbarLayout, title: String?) {
    collapsingToolbarLayout.title = if (TextUtils.isEmpty(title)) "LMusic..." else title
}

@BindingAdapter(value = ["bgPaletteLiveData"])
fun setBGPaletteLiveData(
    imageView: PaletteImageView, liveData: MutableLiveData<Palette>
) {
    imageView.palette = liveData
}

@BindingAdapter(value = ["bgPalette"], requireAll = false)
fun setAppbarBGColor(appBarLayout: AppBarLayout, palette: Palette?) {
    palette ?: return
    setBgColorFromPalette(palette, appBarLayout)
}

@BindingAdapter(value = ["bgPalette"], requireAll = false)
fun setSeekBarBGColor(seekBar: LMusicSeekBar, palette: Palette?) {
    palette ?: return
    seekBar.setThumbColor(getAutomaticColor(palette))
}

@BindingAdapter(value = ["gridGap", "gridSpanCount"], requireAll = true)
fun addGridItemDecoration(recyclerView: RecyclerView, gridGap: Int, gridSpanCount: Int) {
    recyclerView.layoutManager = GridLayoutManager(recyclerView.context, gridSpanCount)
    recyclerView.addItemDecoration(GridItemDecoration(gridGap, gridSpanCount))
}

fun MSongPlayingAdapter.setItems(list: List<MSong>?) {
    list ?: return

    if (this.data.size > 0) {
        val id = this.data[0].songId
        this.setDiffNewData(list.toMutableList()) {
            if (list.isNotEmpty() && id != list.toMutableList()[0].songId) {
                recyclerView.scrollToPosition(0)
            }
        }
    } else {
        this.setDiffNewData(list.toMutableList())
    }
}