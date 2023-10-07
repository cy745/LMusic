package com.lalilu.extension

import android.net.Uri
import android.support.v4.media.MediaMetadataCompat
import com.lalilu.common.base.Playable
import com.lalilu.common.base.Sticker

data class VitsSentence(
    override val mediaId: String,
    override val title: String,
    override val subTitle: String,
    override val durationMs: Long = -1L,
    override val targetUri: Uri,
    override val imageSource: Any? = null,
) : Playable {
    override val sticker: List<Sticker> = emptyList()

    override val metaDataCompat: MediaMetadataCompat = MediaMetadataCompat.Builder()
        .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
        .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, subTitle)
        .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, "unknown")
        .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, mediaId)
        .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, targetUri.toString())
        .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, durationMs)
        .build()
}