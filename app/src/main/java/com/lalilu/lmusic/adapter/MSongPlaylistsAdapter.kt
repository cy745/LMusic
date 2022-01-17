package com.lalilu.lmusic.adapter

import androidx.recyclerview.widget.DiffUtil
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.lalilu.R
import com.lalilu.databinding.ItemPlaylistBinding
import com.lalilu.lmusic.domain.entity.MPlaylist
import javax.inject.Inject

class MSongPlaylistsAdapter @Inject constructor() :
    BaseQuickAdapter<MPlaylist, BaseDataBindingHolder<ItemPlaylistBinding>>(R.layout.item_playlist) {

    init {
        setDiffCallback(DiffPlaylist())
        setEmptyView(R.layout.item_empty_view)
    }

    inner class DiffPlaylist : DiffUtil.ItemCallback<MPlaylist>() {
        override fun areItemsTheSame(
            oldItem: MPlaylist,
            newItem: MPlaylist
        ): Boolean = oldItem.playlistId == newItem.playlistId

        override fun areContentsTheSame(
            oldItem: MPlaylist,
            newItem: MPlaylist
        ): Boolean = oldItem.playlistId == newItem.playlistId &&
                oldItem.playlistTitle == newItem.playlistTitle
    }

    override fun convert(
        holder: BaseDataBindingHolder<ItemPlaylistBinding>,
        item: MPlaylist
    ) {
        val binding = holder.dataBinding ?: return

        binding.title = item.playlistTitle
        binding.coverUri = item.playlistCoverUri
    }
}

