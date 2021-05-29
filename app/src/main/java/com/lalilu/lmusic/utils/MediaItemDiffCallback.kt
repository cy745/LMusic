package com.lalilu.lmusic.utils

import android.support.v4.media.MediaBrowserCompat
import androidx.recyclerview.widget.DiffUtil

class MediaItemDiffCallback(
    private val oldList: List<MediaBrowserCompat.MediaItem>,
    private val newList: List<MediaBrowserCompat.MediaItem>
) :
    DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size
    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].mediaId == newList[newItemPosition].mediaId
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}