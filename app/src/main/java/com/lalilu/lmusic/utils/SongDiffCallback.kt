package com.lalilu.lmusic.utils

import androidx.recyclerview.widget.DiffUtil
import com.lalilu.lmusic.entity.Song

class SongDiffCallback(private val oldList: List<Song>, private val newList: List<Song>) :
    DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size
    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].songId == newList[newItemPosition].songId
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}