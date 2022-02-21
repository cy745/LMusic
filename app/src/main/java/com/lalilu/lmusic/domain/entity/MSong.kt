package com.lalilu.lmusic.domain.entity

import android.net.Uri
import androidx.recyclerview.widget.DiffUtil
import java.util.*

data class MSong(
    val songId: Long,

    val albumId: Long,

    val albumTitle: String,

    val songData: String,

    val songUri: Uri,

    val songTitle: String,

    val songDuration: Long,

    val showingArtist: String = "",

    val songMimeType: String = "",

    val songCreateTime: Date = Date(),

    val songLastPlayTime: Date = Date()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MSong

        if (songId != other.songId) return false

        return true
    }

    override fun hashCode(): Int {
        return songId.hashCode()
    }

    class DiffMSong(
        private val oldList: List<MSong>,
        private val newList: List<MSong>
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].songId == newList[newItemPosition].songId
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]

            return oldItem.songId == newItem.songId &&
                    oldItem.songTitle == newItem.songTitle &&
                    oldItem.songDuration == newItem.songDuration
        }
    }
}
