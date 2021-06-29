package com.lalilu.lmusic.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.DraggableModule
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.lalilu.R
import com.lalilu.databinding.ItemPlayingBinding
import com.lalilu.media.entity.Music

class LMusicPlayingAdapter :
    BaseQuickAdapter<Music, BaseDataBindingHolder<ItemPlayingBinding>>(
        R.layout.item_playing
    ), DraggableModule {

    override fun convert(holder: BaseDataBindingHolder<ItemPlayingBinding>, item: Music) {
        val binding = holder.dataBinding ?: return
        binding.music = item
    }
}