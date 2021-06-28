package com.lalilu.media.entity

import android.net.Uri
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity
data class PlayList(
    @PrimaryKey(autoGenerate = true)
    var playlistId: Long,
    var playlistArt: Uri = Uri.EMPTY,
    var playlistTitle: String = "",
    var playlistCreateTime: Long = System.currentTimeMillis()
)