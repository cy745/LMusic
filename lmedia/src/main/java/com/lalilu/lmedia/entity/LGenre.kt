package com.lalilu.lmedia.entity

import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.lalilu.lmedia.LMedia

data class LGenre(
    override val id: String,
    override val name: String,
    override val songs: List<LSong>
) : MusicParent {
    override var blocked: Boolean = false
}

fun LGenre.toMediaItem(): MediaItem {
    return MediaItem.Builder()
        .setMediaId("${LMedia.GENRE_PREFIX}$id")
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(name)
                .setSubtitle("Songs: ${songs.size}")
                .setIsPlayable(false)
                .setIsBrowsable(true)
                .build()
        )
        .build()
}