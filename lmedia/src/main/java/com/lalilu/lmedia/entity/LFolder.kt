package com.lalilu.lmedia.entity

import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.MediaMetadata.MEDIA_TYPE_FOLDER_MIXED
import com.lalilu.lmedia.LMedia

data class LFolder(
    val path: String,
    override val songs: List<LSong>
) : MusicParent {
    override val name: String by lazy { path.trim('/').substringAfterLast('/') }
    override val id: String = path
    override var blocked: Boolean = false
}


fun LFolder.toMediaItem(): MediaItem {
    return MediaItem.Builder()
        .setMediaId("${LMedia.FOLDER_PREFIX}$id")
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(name)
                .setSubtitle(path)
                .setMediaType(MEDIA_TYPE_FOLDER_MIXED)
                .setIsPlayable(false)
                .setIsBrowsable(true)
                .build()
        )
        .build()
}