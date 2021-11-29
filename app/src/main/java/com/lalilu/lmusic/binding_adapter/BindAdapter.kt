package com.lalilu.lmusic.binding_adapter

import android.content.Context
import android.net.Uri
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.lifecycle.MutableLiveData
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseNodeAdapter
import com.facebook.drawee.view.SimpleDraweeView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.lalilu.R
import com.lalilu.lmusic.adapter.MSongPlayingAdapter
import com.lalilu.lmusic.adapter.node.FirstNode
import com.lalilu.lmusic.domain.entity.MPlaylist
import com.lalilu.lmusic.domain.entity.PlaylistWithSongs
import com.lalilu.lmusic.ui.PaletteDraweeView
import com.lalilu.lmusic.ui.seekbar.LMusicSeekBar
import com.lalilu.lmusic.utils.ColorAnimator.setBgColorFromPalette
import com.lalilu.lmusic.utils.ColorUtils.getAutomaticColor

@BindingAdapter("iconRec")
fun setIcon(imageView: ImageView, string: String) {
    val strings = string.split("/").toTypedArray()
    val result = when (strings[strings.size - 1].uppercase()) {
        "FLAC" -> R.drawable.ic_flac
        "WAV", "X-WAV" -> R.drawable.ic_wav
        "APE" -> R.drawable.ic_ape
        "MPEG", "MP3" -> R.drawable.ic_mp3
        else -> R.drawable.ic_mp3
    }
    imageView.setImageResource(result)
}

@BindingAdapter(value = ["pictureUri", "callerContext"], requireAll = false)
fun setPictureUri(simpleDraweeView: SimpleDraweeView, uri: Uri?, callerContext: Context?) {
    simpleDraweeView.setImageURI(uri, callerContext)
}

@BindingAdapter(value = ["artUri", "callerContext"], requireAll = false)
fun setMSongCoverUri(paletteDraweeView: PaletteDraweeView, uri: Uri?, callerContext: Context?) {
    uri ?: return
    paletteDraweeView.setImageURI(uri, callerContext)
}

@BindingAdapter(value = ["bindTitle"], requireAll = false)
fun setMSongTitle(collapsingToolbarLayout: CollapsingToolbarLayout, title: String?) {
    collapsingToolbarLayout.title = title ?: "LMusic..."
}

@BindingAdapter(value = ["bgPaletteLiveData"], requireAll = false)
fun setBGPaletteLiveData(
    paletteDraweeView: PaletteDraweeView, liveData: MutableLiveData<Palette>
) {
    paletteDraweeView.palette = liveData
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

// TODO: 2021/11/29  setLPlayLists 待修改
@BindingAdapter(value = ["setPlayLists"], requireAll = false)
fun setLPlayLists(recyclerView: RecyclerView, list: List<FirstNode<MPlaylist>>?) {
    recyclerView.adapter ?: return
    list ?: return
    (recyclerView.adapter as BaseNodeAdapter).setList(list)
}

@BindingAdapter(value = ["setPlaylist"], requireAll = false)
fun setPlaylist(recyclerView: RecyclerView, list: PlaylistWithSongs?) {
    recyclerView.adapter ?: return
    list ?: return
    val adapter = recyclerView.adapter as MSongPlayingAdapter
    val oldList = list.songs.toMutableList()

    if (adapter.data.size > 0) {
        val id = adapter.data[0].songId
        adapter.setDiffNewData(oldList) {
            if (id != oldList[0].songId) {
                recyclerView.scrollToPosition(0)
            }
        }
    } else {
        adapter.setDiffNewData(oldList) {
            recyclerView.scrollToPosition(0)
        }
    }
}