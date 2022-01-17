package com.lalilu.lmusic.adapter

import androidx.recyclerview.widget.DiffUtil
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.lalilu.R
import com.lalilu.databinding.ItemAlbumBinding
import com.lalilu.lmusic.domain.entity.MAlbum
import javax.inject.Inject

class MSongAlbumsAdapter @Inject constructor() :
    BaseQuickAdapter<MAlbum, BaseDataBindingHolder<ItemAlbumBinding>>(R.layout.item_album) {

    init {
        setDiffCallback(DiffAlbum())
        setEmptyView(R.layout.item_empty_view)
    }

    inner class DiffAlbum : DiffUtil.ItemCallback<MAlbum>() {
        override fun areItemsTheSame(
            oldItem: MAlbum,
            newItem: MAlbum
        ): Boolean = oldItem.albumId == newItem.albumId

        override fun areContentsTheSame(
            oldItem: MAlbum,
            newItem: MAlbum
        ): Boolean = oldItem.albumId == newItem.albumId &&
                oldItem.albumTitle == newItem.albumTitle &&
                oldItem.albumCoverUri == newItem.albumCoverUri
    }

    override fun convert(
        holder: BaseDataBindingHolder<ItemAlbumBinding>,
        item: MAlbum
    ) {
        val binding = holder.dataBinding ?: return

        binding.title = item.albumTitle
        binding.coverUri = item.albumCoverUri
    }
}

