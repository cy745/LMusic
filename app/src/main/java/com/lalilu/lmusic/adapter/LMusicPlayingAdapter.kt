package com.lalilu.lmusic.adapter

import androidx.recyclerview.widget.DiffUtil
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

    init {
        setDiffCallback(DiffMusic())
        setEmptyView(R.layout.item_empty_view)
    }

    class DiffMusic : DiffUtil.ItemCallback<Music>() {
        override fun areItemsTheSame(oldItem: Music, newItem: Music): Boolean {
            return oldItem.musicId == newItem.musicId
        }

        override fun areContentsTheSame(oldItem: Music, newItem: Music): Boolean {
            return oldItem.musicId == newItem.musicId &&
                    oldItem.musicTitle == newItem.musicTitle
        }
    }
}