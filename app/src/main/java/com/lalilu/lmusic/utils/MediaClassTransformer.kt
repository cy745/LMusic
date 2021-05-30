package com.lalilu.lmusic.utils

import android.media.MediaMetadata
import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import com.lalilu.lmusic.service2.MusicService

fun Bundle.toMediaMeta(): MediaMetadataCompat {
    return try {
        MediaMetadataCompat.Builder()
            .putString(
                MediaMetadata.METADATA_KEY_TITLE,
                this.getString(MediaMetadata.METADATA_KEY_TITLE)
            )
            .putString(
                MediaMetadataCompat.METADATA_KEY_MEDIA_URI,
                this.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI)
            )
            .putString(
                MediaMetadata.METADATA_KEY_MEDIA_ID,
                this.getString(MediaMetadata.METADATA_KEY_MEDIA_ID)
            )
            .putString(
                MediaMetadata.METADATA_KEY_ARTIST,
                this.getString(MediaMetadata.METADATA_KEY_ARTIST)
            )
            .putString(
                MediaMetadata.METADATA_KEY_ART_URI,
                this.getString(MediaMetadata.METADATA_KEY_ART_URI)
            )
            .putString(
                MediaMetadata.METADATA_KEY_ALBUM,
                this.getString(MediaMetadata.METADATA_KEY_ALBUM)
            )
            .putLong(
                MediaMetadata.METADATA_KEY_DURATION,
                this.getLong(MediaMetadata.METADATA_KEY_DURATION)
            )
            .putString(MusicService.Song_Type, this.getString(MusicService.Song_Type))
            .build()
    } catch (e: java.lang.Exception) {
        MediaMetadataCompat.Builder().build()
    }
}