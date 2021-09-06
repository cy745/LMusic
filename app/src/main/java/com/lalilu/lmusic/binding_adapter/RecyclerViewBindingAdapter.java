package com.lalilu.lmusic.binding_adapter;

import android.content.Context;
import android.net.Uri;

import androidx.databinding.BindingAdapter;
import androidx.lifecycle.MutableLiveData;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseNodeAdapter;
import com.google.android.material.appbar.AppBarLayout;
import com.lalilu.lmusic.adapter.LMusicPlayingAdapter;
import com.lalilu.lmusic.adapter.node.FirstNode;
import com.lalilu.lmusic.ui.PaletteDraweeView;
import com.lalilu.lmusic.ui.seekbar.LMusicSeekBar;
import com.lalilu.lmusic.utils.ColorAnimator;
import com.lalilu.lmusic.domain.entity.LPlaylist;
import com.lalilu.lmusic.utils.ColorUtils;

import net.opacapp.multilinecollapsingtoolbar.CollapsingToolbarLayout;

import java.util.List;
import java.util.Objects;

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

    @BindingAdapter(value = {"bgPaletteLiveData"}, requireAll = false)
    public static void setBGPaletteLiveData(PaletteDraweeView paletteDraweeView, MutableLiveData<Palette> liveData) {
        paletteDraweeView.setPalette(liveData);
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

    @BindingAdapter(value = {"setLPlaylist"}, requireAll = false)
    public static void setLPlaylist(RecyclerView recyclerView, LPlaylist list) {
        if (recyclerView.getAdapter() != null && list != null) {
            LMusicPlayingAdapter adapter = (LMusicPlayingAdapter) recyclerView.getAdapter();

            if (adapter.getData().size() > 0) {
                long id = adapter.getData().get(0).getMId();
                adapter.setDiffNewData(list.getSongs(), () -> {
                    if (id != Objects.requireNonNull(list.getSongs()).get(0).getMId()) {
                        recyclerView.scrollToPosition(0);
                    }
                });
            } else {
                adapter.setDiffNewData(list.getSongs(), () -> recyclerView.scrollToPosition(0));
            }
        }
    }

    @BindingAdapter(value = {"setLPlayLists"}, requireAll = false)
    public static void setLPlayLists(RecyclerView recyclerView, List<FirstNode<LPlaylist>> list) {
        if (recyclerView.getAdapter() == null || list == null) return;
        ((BaseNodeAdapter) recyclerView.getAdapter()).setList(list);
    }
}
