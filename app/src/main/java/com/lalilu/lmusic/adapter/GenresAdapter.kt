package com.lalilu.lmusic.adapter

import androidx.media3.common.MediaItem
import androidx.recyclerview.widget.DiffUtil
import com.lalilu.R
import com.lalilu.databinding.ItemGenreBinding
import javax.inject.Inject

class GenresAdapter @Inject constructor() :
    BaseAdapter<MediaItem, ItemGenreBinding>(R.layout.item_genre) {

    override val itemCallback: DiffUtil.ItemCallback<MediaItem>
        get() = object : DiffUtil.ItemCallback<MediaItem>() {
            override fun areItemsTheSame(oldItem: MediaItem, newItem: MediaItem): Boolean {
                return oldItem.mediaId == newItem.mediaId
            }

            override fun areContentsTheSame(oldItem: MediaItem, newItem: MediaItem): Boolean {
                return oldItem.mediaId == newItem.mediaId &&
                        oldItem.mediaMetadata.title == newItem.mediaMetadata.title
            }
        }

    override fun onBind(binding: ItemGenreBinding, item: MediaItem, position: Int) {
        binding.title = item.mediaMetadata.genre.toString()
    }
}
