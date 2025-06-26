package com.lalilu.lmedia.entity

import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import com.lalilu.common.base.SourceType
import com.lalilu.lmedia.extension.albumCoverUri
import com.lalilu.lmedia.extension.sortable.Sortable

data class Metadata(
    val title: String,
    val album: String,
    val artist: String,
    val albumArtist: String,
    val composer: String,
    val lyricist: String,
    val comment: String,
    val genre: String,
    val track: String,
    val disc: String,
    val date: String,
    val duration: Long,
    val dateAdded: Long,
    val dateModified: Long
)

data class FileInfo(
    val mimeType: String,
    val directoryPath: String,
    val pathStr: String?,
    val fileName: String?,
    val size: Long,
    val bitrate: Int = 0,
)

data class LSong(
    override val id: String,
    val metadata: Metadata,
    val fileInfo: FileInfo,
    val uri: Uri,
    val sourceType: SourceType,
    val albumId: String? = null,
) : Item {
    override val name = metadata.title
    override var blocked: Boolean = false

    var album: LAlbum? = null
        private set
    var folder: LFolder? = null
        private set
    val artists: LinkedHashSet<LArtist> = linkedSetOf()
    val genres: LinkedHashSet<LGenre> = linkedSetOf()

    var artworkUri: Uri? = null
    val albumCoverUri: Uri? = album?.coverUri
        ?: albumId?.toLongOrNull()
            ?.takeIf { it >= 0 }
            ?.albumCoverUri()

    override fun <T : Item> link(item: T) {
        when (item) {
            is LAlbum -> album = item
            is LFolder -> folder = item
            is LArtist -> artists.add(item)
            is LGenre -> genres.add(item)
        }
    }

    @Suppress("UNCHECKED_CAST", "IMPLICIT_CAST_TO_ANY")
    override fun <T : Any> getValueBy(key: String): T? {
        return when (key) {
            Sortable.COMPARE_KEY_TITLE -> metadata.title
            Sortable.COMPARE_KEY_SUB_TITLE -> metadata.artist
            Sortable.COMPARE_KEY_CREATE_TIME -> metadata.dateAdded
            Sortable.COMPARE_KEY_MODIFY_TIME -> metadata.dateModified
            Sortable.COMPARE_KEY_CONTENT_TYPE -> fileInfo.mimeType
            Sortable.COMPARE_KEY_FILE_SIZE -> fileInfo.size
            Sortable.COMPARE_KEY_DISK_NUMBER -> metadata.disc
            Sortable.COMPARE_KEY_TRACK_NUMBER -> metadata.track
            Sortable.COMPARE_KEY_DURATION -> metadata.duration
            else -> super.getValueBy<T>(key)
        } as? T?
    }

    override fun getMatchSource(): String = "$name ${metadata.artist}"
}


@OptIn(UnstableApi::class)
fun LSong.toMediaItem(): MediaItem {
    return MediaItem.Builder()
        .setMediaId(id)
        .setUri(uri)
        .setMimeType(fileInfo.mimeType)
        .setRequestMetadata(
            MediaItem.RequestMetadata.Builder()
                .setMediaUri(uri)
                .build()
        )
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(name)
                .setDisplayTitle(name)
                .setArtist(metadata.artist)
                .setSubtitle(metadata.artist)
                .setAlbumTitle(metadata.album)
                .setAlbumArtist(metadata.albumArtist)
                .setDurationMs(metadata.duration)
                .setMediaType(MediaMetadata.MEDIA_TYPE_MUSIC)
                .setIsPlayable(true)
                .setIsBrowsable(true)
                .build()
        )
        .build()
}