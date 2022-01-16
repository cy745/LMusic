package com.lalilu.lmusic.adapter

import androidx.recyclerview.widget.DiffUtil
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.lalilu.R
import com.lalilu.databinding.ItemPlaylistExpandBinding
import com.lalilu.lmusic.domain.entity.FullSongInfo
import javax.inject.Inject

class MSongPlaylistAdapter @Inject constructor() :
    BaseQuickAdapter<FullSongInfo, BaseDataBindingHolder<ItemPlaylistExpandBinding>>(R.layout.item_playlist_expand) {

    init {
        setDiffCallback(DiffSong())
        setEmptyView(R.layout.item_empty_view)
    }

    inner class DiffSong : DiffUtil.ItemCallback<FullSongInfo>() {
        override fun areItemsTheSame(
            oldItem: FullSongInfo,
            newItem: FullSongInfo
        ): Boolean = oldItem.song.songId == newItem.song.songId

        override fun areContentsTheSame(
            oldItem: FullSongInfo,
            newItem: FullSongInfo
        ): Boolean = oldItem.song.songId == newItem.song.songId &&
                oldItem.song.songTitle == newItem.song.songTitle
    }

    override fun convert(
        holder: BaseDataBindingHolder<ItemPlaylistExpandBinding>,
        item: FullSongInfo
    ) {
        val binding = holder.dataBinding ?: return

        binding.title = item.song.songTitle
    }
}

