package com.lalilu.lmusic.domain.entity

import android.content.ContentUris
import android.net.Uri

data class MAlbum(
    val albumId: Long,
    val albumTitle: String
) {
    val albumCoverUri: Uri
        get() = ContentUris.withAppendedId(
            Uri.parse("content://media/external/audio/albumart/"),
            this.albumId
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MAlbum

        if (albumId != other.albumId) return false

        return true
    }

    override fun hashCode(): Int {
        return albumId.hashCode()
    }
}