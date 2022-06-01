package com.lalilu.lmusic.datasource.extensions

import android.os.Bundle
import android.provider.MediaStore
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata

fun Bundle.setAlbumId(albumId: Long): Bundle {
    putLong(MediaStore.Audio.Media.ALBUM_ID, albumId)
    return this
}

fun Bundle.setArtistId(artistId: Long): Bundle {
    putLong(MediaStore.Audio.Media.ARTIST_ID, artistId)
    return this
}

fun Bundle.setDuration(duration: Long): Bundle {
    putLong(MediaStore.Audio.Media.DURATION, duration)
    return this
}

fun Bundle.setSongData(songData: String): Bundle {
    putString(MediaStore.Audio.Media.DATA, songData)
    return this
}

fun MediaMetadata.getDuration(): Long {
    return this.extras?.getLong(MediaStore.Audio.Media.DURATION) ?: 0L
}

fun MediaMetadata.getArtistId(): Long {
    return this.extras?.getLong(MediaStore.Audio.Media.ARTIST_ID) ?: 0L
}

fun MediaMetadata.getAlbumId(): Long {
    return this.extras?.getLong(MediaStore.Audio.Media.ALBUM_ID) ?: 0L
}

fun MediaMetadata.getSongData(): String? {
    return this.extras?.getString(MediaStore.Audio.Media.DATA)
}

fun MediaItem.partCopy(): MediaItem {
    return MediaItem.Builder()
        .setUri(this.mediaMetadata.mediaUri)
        .setMediaMetadata(this.mediaMetadata)
        .setMimeType(this.localConfiguration?.mimeType)
        .setMediaId(this.mediaId)
        .setTag(System.currentTimeMillis())
        .build()
}