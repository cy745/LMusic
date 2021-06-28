package com.lalilu.media.entity

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Music(
    @PrimaryKey(autoGenerate = true)
    var musicId: Long,
    var musicTitle: String = "",
    var musicSize: Long = 0,
    var musicDuration: Long = 0,
    var musicArtist: String = "",
    var musicMimeType: String = "",
    var musicUri: Uri = Uri.EMPTY,
    var albumTitle: String = "",
    var musicArtUri: Uri = Uri.EMPTY,
)