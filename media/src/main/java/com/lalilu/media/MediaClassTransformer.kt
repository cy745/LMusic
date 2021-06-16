package com.lalilu.media

import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import com.lalilu.common.GlobalCommon

fun MediaMetadataCompat.toMediaItem(): MediaBrowserCompat.MediaItem {
    val description = MediaDescriptionCompat.Builder()
        .setIconUri(this.description.iconUri)
        .setMediaUri(this.description.mediaUri)
        .setTitle(this.description.title)
        .setMediaId(this.description.mediaId)
        .setSubtitle(this.description.subtitle)
        .setDescription(this.description.description)
        .setSubtitle(this.description.subtitle)
        .setExtras(Bundle().also {
            it.putString(
                MediaMetadataCompat.METADATA_KEY_TITLE,
                this.bundle.getString(MediaMetadataCompat.METADATA_KEY_TITLE)
            )
            it.putString(
                MediaMetadataCompat.METADATA_KEY_MEDIA_URI,
                this.bundle.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI)
            )
            it.putString(
                MediaMetadataCompat.METADATA_KEY_MEDIA_ID,
                this.bundle.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)
            )
            it.putString(
                MediaMetadataCompat.METADATA_KEY_ARTIST,
                this.bundle.getString(MediaMetadataCompat.METADATA_KEY_ARTIST)
            )
            it.putString(
                MediaMetadataCompat.METADATA_KEY_ART_URI,
                this.bundle.getString(MediaMetadataCompat.METADATA_KEY_ART_URI)
            )
            it.putString(
                MediaMetadataCompat.METADATA_KEY_ALBUM,
                this.bundle.getString(MediaMetadataCompat.METADATA_KEY_ALBUM)
            )
            it.putLong(
                MediaMetadataCompat.METADATA_KEY_DURATION,
                this.bundle.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)
            )
            it.putString(
                GlobalCommon.MEDIA_MIME_TYPE,
                this.bundle.getString(GlobalCommon.MEDIA_MIME_TYPE)
            )
        }).build()
    return MediaBrowserCompat.MediaItem(description, MediaBrowserCompat.MediaItem.FLAG_PLAYABLE)
}
