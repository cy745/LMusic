package com.lalilu.media.entity

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Playlist(
    @PrimaryKey(autoGenerate = true)
    var playlistId: Long = 0,
    var playlistArt: Uri? = Uri.EMPTY,
    var playlistTitle: String = "",
    var playlistCreateTime: Long = System.currentTimeMillis(),
    var playlistOrder: ArrayList<Long> = ArrayList()
)