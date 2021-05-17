package com.lalilu.lmusic.entity

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "album")
class Album {
    @PrimaryKey(autoGenerate = true)
    var albumId: Long = 0
    var albumTitle: String = ""
    var albumUri: Uri = Uri.EMPTY
    var albumArtist: String = ""
}