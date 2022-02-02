package com.lalilu.lmusic.adapter

import androidx.recyclerview.widget.DiffUtil
import com.lalilu.R
import com.lalilu.databinding.ItemPlaylistExpandBinding
import com.lalilu.lmusic.domain.entity.MSong
import javax.inject.Inject

class ListAdapter @Inject constructor() :
    BaseAdapter<MSong, ItemPlaylistExpandBinding>(R.layout.item_playlist_expand) {

    override val itemCallback: DiffUtil.ItemCallback<MSong>
        get() = object : DiffUtil.ItemCallback<MSong>() {
            override fun areItemsTheSame(oldItem: MSong, newItem: MSong): Boolean {
                return oldItem.songId == newItem.songId
            }

            override fun areContentsTheSame(oldItem: MSong, newItem: MSong): Boolean {
                return oldItem.songId == newItem.songId &&
                        oldItem.songTitle == newItem.songTitle
            }
        }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val binding = holder.binding
        val item = data[position]

        binding.title = item.songTitle
    }
}

