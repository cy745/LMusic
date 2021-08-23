package com.lalilu.lmusic.binding_adapter;

import android.content.Context;
import android.net.Uri;

import androidx.databinding.BindingAdapter;
import androidx.lifecycle.MutableLiveData;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseNodeAdapter;
import com.google.android.material.appbar.AppBarLayout;
import com.lalilu.common.ColorUtils;
import com.lalilu.lmusic.adapter.LMusicPlayingAdapter;
import com.lalilu.lmusic.adapter.node.FirstNode;
import com.lalilu.lmusic.ui.PaletteDraweeView;
import com.lalilu.lmusic.ui.seekbar.LMusicSeekBar;
import com.lalilu.lmusic.utils.ColorAnimator;
import com.lalilu.media.entity.Music;
import com.lalilu.media.entity.Playlist;

import net.opacapp.multilinecollapsingtoolbar.CollapsingToolbarLayout;

import java.util.List;

public class RecyclerViewBindingAdapter {

    @BindingAdapter(value = {"artUri", "callerContext"})
    public static void setNowPlayingMusicArtUri(
            PaletteDraweeView paletteDraweeView,
            Uri uri, Context callerContext) {
        if (uri == null) return;
        paletteDraweeView.setImageURI(uri, callerContext);
    }

    @BindingAdapter(value = {"bindTitle"}, requireAll = false)
    public static void setNowPlayingMusicTitle(CollapsingToolbarLayout collapsingToolbarLayout, String title) {
        collapsingToolbarLayout.setTitle(title);
    }

    @BindingAdapter(value = {"bgColorLiveData"}, requireAll = false)
    public static void setBGColorLiveData(PaletteDraweeView paletteDraweeView, MutableLiveData<Palette> liveData) {
        paletteDraweeView.setColorLiveData(liveData);
    }

    @BindingAdapter(value = {"bgPalette"}, requireAll = false)
    public static void setAppbarBGColor(AppBarLayout appBarLayout, Palette palette) {
        if (palette == null) return;
        ColorAnimator.Companion.setBgColorFromPalette(palette, appBarLayout);
    }

    @BindingAdapter(value = {"bgPalette"}, requireAll = false)
    public static void setSeekBarBGColor(LMusicSeekBar seekBar, Palette palette) {
        if (palette == null) return;
        seekBar.setThumbColor(ColorUtils.Companion.getAutomaticColor(palette));
    }

    @BindingAdapter(value = {"setMusicList"}, requireAll = false)
    public static void setMusicList(RecyclerView recyclerView, List<Music> list) {
        if (recyclerView.getAdapter() != null && list != null) {
            LMusicPlayingAdapter adapter = (LMusicPlayingAdapter) recyclerView.getAdapter();

            if (adapter.getData().size() > 0) {
                long id = adapter.getData().get(0).getMusicId();
                adapter.setDiffNewData(list, () -> {
                    if (id != list.get(0).getMusicId()) {
                        recyclerView.scrollToPosition(0);
                    }
                });
            } else {
                adapter.setDiffNewData(list, () -> recyclerView.scrollToPosition(0));
            }
        }
    }

    @BindingAdapter(value = {"setPlayList"}, requireAll = false)
    public static void setPlayList(RecyclerView recyclerView, List<FirstNode<Playlist>> list) {
        if (recyclerView.getAdapter() == null || list == null) return;
        ((BaseNodeAdapter) recyclerView.getAdapter()).setList(list);
    }
}
