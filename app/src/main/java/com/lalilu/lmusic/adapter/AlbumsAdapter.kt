package com.lalilu.lmusic.adapter

import androidx.recyclerview.widget.DiffUtil
import com.lalilu.R
import com.lalilu.databinding.ItemAlbumBinding
import com.lalilu.lmusic.domain.entity.MAlbum
import javax.inject.Inject

class AlbumsAdapter @Inject constructor() :
    BaseAdapter<MAlbum, ItemAlbumBinding>(R.layout.item_album) {

    override val itemCallback: DiffUtil.ItemCallback<MAlbum>
        get() = object : DiffUtil.ItemCallback<MAlbum>() {
            override fun areItemsTheSame(oldItem: MAlbum, newItem: MAlbum): Boolean {
                return oldItem.albumId == newItem.albumId
            }

            override fun areContentsTheSame(oldItem: MAlbum, newItem: MAlbum): Boolean {
                return oldItem.albumId == newItem.albumId &&
                        oldItem.albumTitle == newItem.albumTitle &&
                        oldItem.albumCoverUri == newItem.albumCoverUri
            }
        }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val binding = holder.binding
        val item = data[position]

        binding.album = item
        binding.root.setOnClickListener {
            onItemClickListener?.invoke(item)
        }
    }
}