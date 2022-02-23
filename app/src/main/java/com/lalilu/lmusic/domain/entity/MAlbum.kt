package com.lalilu.lmusic.domain.entity

import android.content.ContentUris
import android.net.Uri

data class MAlbum(
    val albumId: Long,
    val albumTitle: String,
    val artistId: Long,
    val artistName: String,
) {
    val albumCoverUri: Uri
        get() = ContentUris.withAppendedId(
            Uri.parse("content://media/external/audio/albumart/"),
            this.albumId
        )
}