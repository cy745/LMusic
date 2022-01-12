package com.lalilu.lmusic.utils

import android.content.SharedPreferences
import android.database.Cursor
import android.graphics.Color
import android.provider.MediaStore
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.palette.graphics.Palette
import com.lalilu.lmusic.Config
import com.lalilu.lmusic.domain.entity.MSong

fun Palette?.getAutomaticColor(): Int {
    if (this == null) return Color.DKGRAY
    var oldColor = this.getDarkVibrantColor(Color.LTGRAY)
    if (ColorUtils.isLightColor(oldColor))
        oldColor = this.getDarkMutedColor(Color.LTGRAY)
    return oldColor
}

fun Cursor.getSongId(): Long {
    val index = this.getColumnIndex(MediaStore.Audio.Media._ID)
    return if (index < 0) return 0 else this.getLong(index)
}

fun Cursor.getSongTitle(): String {
    val index = this.getColumnIndex(MediaStore.Audio.Media.TITLE)
    return if (index < 0) "" else this.getString(index)
}

fun Cursor.getAlbumId(): Long {
    val index = this.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)
    return if (index < 0) return 0 else this.getLong(index)
}

fun Cursor.getAlbumTitle(): String {
    val index = this.getColumnIndex(MediaStore.Audio.Media.ALBUM)
    return if (index < 0) "" else this.getString(index)
}

fun Cursor.getArtist(): String {
    val index = this.getColumnIndex(MediaStore.Audio.Media.ARTIST)
    return if (index < 0) "" else this.getString(index)
}

fun Cursor.getArtists(): List<String> {
    return this.getArtist().split("/")
}

fun Cursor.getSongSize(): Long {
    val index = this.getColumnIndex(MediaStore.Audio.Media.SIZE)
    return if (index < 0) return 0 else this.getLong(index)
}

fun Cursor.getSongData(): String {
    val index = this.getColumnIndex(MediaStore.Audio.Media.DATA)
    return if (index < 0) "" else this.getString(index)
}

fun Cursor.getSongDuration(): Long {
    val index = this.getColumnIndex(MediaStore.Audio.Media.DURATION)
    return if (index < 0) return 0 else this.getLong(index)
}

fun Cursor.getSongMimeType(): String {
    val index = this.getColumnIndex(MediaStore.Audio.Media.MIME_TYPE)
    return if (index < 0) "" else this.getString(index)
}

fun MSong.toMediaMetadataCompat(): MediaMetadataCompat {
    val metadata = MediaMetadataCompat.Builder()
        .putString(MediaMetadataCompat.METADATA_KEY_TITLE, this.songTitle)
        .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, this.showingArtist)
        .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, this.albumTitle)
        .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, this.songId.toString())
        .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, this.songDuration)
        .putString(MediaMetadataCompat.METADATA_KEY_ART_URI, this.songCoverUri.toString())
        .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, this.songUri.toString())
        .putString(Config.MEDIA_MIME_TYPE, this.songMimeType)
//        if (this.songCoverUri != Uri.EMPTY) {
//            metadata.putBitmap(
//                MediaMetadataCompat.METADATA_KEY_ART,
//                BitmapUtils.loadBitmapFromUri(this.songCoverUri, 500)
//            )
//        }
    return metadata.build()
}

fun MediaMetadataCompat.toMediaItem(): MediaBrowserCompat.MediaItem {
    return MediaBrowserCompat.MediaItem(
        MediaDescriptionCompat.Builder()
            .setTitle(this.description.title)
            .setMediaId(this.description.mediaId)
            .setSubtitle(this.description.subtitle)
            .setDescription(this.description.description)
            .setIconUri(this.description.iconUri)
            .setMediaUri(this.description.mediaUri)
            .setExtras(this.bundle).build(),
        MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
    )
}

fun MediaMetadataCompat.saveTo(pref: SharedPreferences) {
    with(pref.edit()) {
        this.putString(
            MediaMetadataCompat.METADATA_KEY_MEDIA_ID,
            this@saveTo.description.mediaId
        )
        this.putString(
            MediaMetadataCompat.METADATA_KEY_TITLE,
            this@saveTo.description.title.toString()
        )
        this.putString(
            MediaMetadataCompat.METADATA_KEY_ART_URI,
            this@saveTo.description.iconUri.toString()
        )
        this.apply()
    }
}

fun PlaybackStateCompat.saveTo(pref: SharedPreferences) {
    with(pref.edit()) {
        this.putLong(Config.LAST_POSITION, this@saveTo.position)
        this.apply()
    }
}

fun SharedPreferences.getLastPlaybackState(): PlaybackStateCompat {
    return PlaybackStateCompat.Builder()
        .setState(
            PlaybackStateCompat.STATE_STOPPED,
            this.getLong(Config.LAST_POSITION, 0L),
            1.0f
        ).build()
}

fun SharedPreferences.getLastMediaMetadata(): MediaMetadataCompat {
    return MediaMetadataCompat.Builder()
        .putString(
            MediaMetadataCompat.METADATA_KEY_TITLE,
            this.getString(MediaMetadataCompat.METADATA_KEY_TITLE, "")
        )
        .putString(
            MediaMetadataCompat.METADATA_KEY_MEDIA_ID,
            this.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, "")
        )
        .putString(
            MediaMetadataCompat.METADATA_KEY_ART_URI,
            this.getString(MediaMetadataCompat.METADATA_KEY_ART_URI, "")
        )
        .build()
}