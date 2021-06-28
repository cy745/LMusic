package com.lalilu.lmusic.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.DraggableModule
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.lalilu.R
import com.lalilu.databinding.ItemSongMediaItemBinding
import com.lalilu.media.entity.Music

class LMusicPlayingAdapter :
    BaseQuickAdapter<Music, BaseDataBindingHolder<ItemSongMediaItemBinding>>(
        R.layout.item_song_media_item
    ), DraggableModule {

    override fun convert(holder: BaseDataBindingHolder<ItemSongMediaItemBinding>, item: Music) {
        val binding = holder.dataBinding ?: return
        binding.media = item
    }
}