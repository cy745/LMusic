package com.lalilu.lmusic.adapter

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import androidx.recyclerview.widget.DiffUtil
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.DraggableModule
import com.chad.library.adapter.base.viewholder.BaseDataBindingHolder
import com.lalilu.R
import com.lalilu.databinding.ItemPlayingBinding
import com.lalilu.lmusic.Config
import javax.inject.Inject

class MSongPlayingAdapter @Inject constructor() :
    BaseQuickAdapter<MediaBrowserCompat.MediaItem, BaseDataBindingHolder<ItemPlayingBinding>>(
        R.layout.item_playing
    ), DraggableModule {

    init {
        setDiffCallback(DiffSong())
        setEmptyView(R.layout.item_empty_view)
    }

    inner class DiffSong : DiffUtil.ItemCallback<MediaBrowserCompat.MediaItem>() {
        override fun areItemsTheSame(
            oldItem: MediaBrowserCompat.MediaItem,
            newItem: MediaBrowserCompat.MediaItem
        ): Boolean = oldItem.description.mediaId == newItem.description.mediaId

        override fun areContentsTheSame(
            oldItem: MediaBrowserCompat.MediaItem,
            newItem: MediaBrowserCompat.MediaItem
        ): Boolean = oldItem.description.mediaId == newItem.description.mediaId &&
                oldItem.description.title == newItem.description.title
    }

    override fun convert(
        holder: BaseDataBindingHolder<ItemPlayingBinding>,
        item: MediaBrowserCompat.MediaItem
    ) {
        val binding = holder.dataBinding ?: return

        val duration = item.description.extras?.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)
        val mimeType = item.description.extras?.getString(Config.MEDIA_MIME_TYPE)

        binding.title = item.description.title.toString()
        binding.artist = item.description.subtitle.toString()
        binding.coverUri = item.description.iconUri
        binding.duration = duration
        binding.mimeType = mimeType
    }
}