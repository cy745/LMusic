package com.lalilu.lmusic.adapter

import android.support.v4.media.MediaBrowserCompat
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.DraggableModule
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.lalilu.R
import com.lalilu.databinding.ItemSongMediaItemBinding

class LMusicPlayingAdapter :
    BaseQuickAdapter<MediaBrowserCompat.MediaItem, BaseDataBindingHolder<ItemSongMediaItemBinding>>(
        R.layout.item_song_media_item
    ), DraggableModule {

    override fun convert(
        holder: BaseDataBindingHolder<ItemSongMediaItemBinding>,
        item: MediaBrowserCompat.MediaItem
    ) {
        val binding = holder.dataBinding ?: return
        binding.mediaItem = item
    }
}