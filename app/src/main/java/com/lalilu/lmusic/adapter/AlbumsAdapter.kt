package com.lalilu.lmusic.adapter

import android.annotation.SuppressLint
import androidx.media3.common.MediaItem
import androidx.recyclerview.widget.DiffUtil
import com.lalilu.R
import com.lalilu.databinding.ItemAlbumBinding
import javax.inject.Inject

class AlbumsAdapter @Inject constructor() :
    BaseAdapter<MediaItem, ItemAlbumBinding>(R.layout.item_album) {

    override val itemCallback: DiffUtil.ItemCallback<MediaItem>
        get() = object : DiffUtil.ItemCallback<MediaItem>() {
            @SuppressLint("UnsafeOptInUsageError")
            override fun areItemsTheSame(oldItem: MediaItem, newItem: MediaItem): Boolean {
                return oldItem.mediaId == newItem.mediaId
            }

            @SuppressLint("UnsafeOptInUsageError")
            override fun areContentsTheSame(oldItem: MediaItem, newItem: MediaItem): Boolean {
                return oldItem.mediaId == newItem.mediaId &&
                        oldItem.mediaMetadata.title == newItem.mediaMetadata.title &&
                        oldItem.mediaMetadata.albumTitle == newItem.mediaMetadata.albumTitle &&
                        oldItem.mediaMetadata.artworkUri == newItem.mediaMetadata.artworkUri
            }
        }

    override fun onBind(binding: ItemAlbumBinding, item: MediaItem) {
        binding.mediaItem = item
    }
}