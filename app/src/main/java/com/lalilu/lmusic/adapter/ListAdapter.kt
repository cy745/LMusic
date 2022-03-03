package com.lalilu.lmusic.adapter

import android.annotation.SuppressLint
import androidx.media3.common.MediaItem
import androidx.recyclerview.widget.DiffUtil
import com.lalilu.R
import com.lalilu.databinding.ItemListItemBinding
import javax.inject.Inject

@SuppressLint("UnsafeOptInUsageError")
class ListAdapter @Inject constructor() :
    BaseAdapter<MediaItem, ItemListItemBinding>(R.layout.item_list_item) {

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

    override fun onBind(binding: ItemListItemBinding, item: MediaItem) {
        binding.mediaItem = item
    }
}

