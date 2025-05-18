package com.lalilu.lmedia.entity

import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.MediaMetadata.MEDIA_TYPE_ARTIST
import com.lalilu.lmedia.extension.sortable.Sortable
import com.lalilu.lmedia.LMedia

data class LArtist(
    override val name: String,
    override val songs: List<LSong>
) : MusicParent {
    override val id: String = name
    override var blocked: Boolean = false

    @Suppress("UNCHECKED_CAST", "IMPLICIT_CAST_TO_ANY")
    override fun <T : Any> getValueBy(key: String): T? {
        return when (key) {
            Sortable.COMPARE_KEY_TITLE -> name
            else -> super.getValueBy<T>(key)
        } as? T?
    }
}

/**
 * 根据特殊的符号将[LArtist.name]拆分成多个[LArtist]
 */
fun List<LArtist>.separate(): List<LArtist> = flatMap {
    it.name.split('/', ';', '、', ',', '，').map { name ->
        LArtist(
            name = name.trim(),
            songs = it.songs
        )
    }
}

/**
 * 根据[LArtist.name]将多个重名的[LArtist]合并
 */
fun List<LArtist>.merge(): List<LArtist> = groupBy { it.name }.map { entry ->
    LArtist(
        name = entry.key,
        songs = entry.value.flatMap { it.songs }
    )
}

fun LArtist.toMediaItem(): MediaItem {
    return MediaItem.Builder()
        .setMediaId("${LMedia.ARTIST_PREFIX}$id")
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(name)
                .setSubtitle("Songs: ${songs.size}")
                .setMediaType(MEDIA_TYPE_ARTIST)
                .setIsPlayable(false)
                .setIsBrowsable(true)
                .build()
        )
        .build()
}