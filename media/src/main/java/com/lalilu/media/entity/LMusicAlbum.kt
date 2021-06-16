package com.lalilu.media.entity

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lmusic_album")
class LMusicAlbum {
    @PrimaryKey(autoGenerate = true)
    var albumId: Long = 0
    var albumTitle: String = ""
    var albumUri: Uri = Uri.EMPTY
    var albumArtist: String = ""
}