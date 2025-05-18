package com.lalilu.lmedia.entity

import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.MediaMetadata.MEDIA_TYPE_ALBUM
import com.lalilu.lmedia.extension.albumCoverUri
import com.lalilu.lmedia.LMedia

data class LAlbum(
    override val id: String,
    override val name: String,
    override val songs: List<LSong>,
    val artistName: String? = null
) : MusicParent {
    override var blocked: Boolean = false

    override fun getMatchSource(): String = "$name ${artistName ?: ""}"

    val coverUri: Uri?
        get() = id.toLongOrNull()
            ?.takeIf { it >= 0 }
            ?.albumCoverUri()
}

fun List<LAlbum>.merge(): List<LAlbum> {
    return groupBy { it.name }.values.map { list ->
        list[0].copy(songs = list.flatMap { it.songs })
    }
}

fun LAlbum.toMediaItem(): MediaItem {
    return MediaItem.Builder()
        .setMediaId("${LMedia.ALBUM_PREFIX}$id")
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(name)
                .setSubtitle("Songs: ${songs.size}")
                .setMediaType(MEDIA_TYPE_ALBUM)
                .setIsPlayable(false)
                .setIsBrowsable(true)
                .build()
        )
        .build()
}