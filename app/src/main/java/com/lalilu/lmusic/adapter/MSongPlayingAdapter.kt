package com.lalilu.lmusic.adapter

import androidx.recyclerview.widget.DiffUtil
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.DraggableModule
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.lalilu.R
import com.lalilu.databinding.ItemPlayingBinding
import com.lalilu.lmusic.domain.entity.MSong
import javax.inject.Inject

class MSongPlayingAdapter @Inject constructor() :
    BaseQuickAdapter<MSong, BaseDataBindingHolder<ItemPlayingBinding>>(
        R.layout.item_playing
    ), DraggableModule {

    init {
        setDiffCallback(DiffSong())
        setEmptyView(R.layout.item_empty_view)
    }

    inner class DiffSong : DiffUtil.ItemCallback<MSong>() {
        override fun areItemsTheSame(
            oldItem: MSong,
            newItem: MSong
        ): Boolean = oldItem.songId == newItem.songId

        override fun areContentsTheSame(
            oldItem: MSong,
            newItem: MSong
        ): Boolean = oldItem.songId == newItem.songId &&
                oldItem.songTitle == newItem.songTitle
    }

    override fun convert(
        holder: BaseDataBindingHolder<ItemPlayingBinding>,
        item: MSong
    ) {
        val binding = holder.dataBinding ?: return

        binding.title = item.songTitle
        binding.artist = item.showingArtist
        binding.coverUri = item.songCoverUri
        binding.duration = item.songDuration
        binding.mimeType = item.songMimeType
    }
}