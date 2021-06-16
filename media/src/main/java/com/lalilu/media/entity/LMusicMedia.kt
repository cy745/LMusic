package com.lalilu.media.entity

import android.net.Uri
import android.support.v4.media.MediaMetadataCompat
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.lalilu.common.GlobalCommon.MEDIA_MIME_TYPE

@Entity(tableName = "lmusic_media")
class LMusicMedia {
    @PrimaryKey(autoGenerate = true)
    var mediaId: Long = 0
    var mediaTitle: String = ""
    var mediaSize: Long = 0
    var mediaUri: Uri = Uri.EMPTY
    var mediaArtist: String = ""
    var mediaDuration: Long = 0
    var mediaMimeType: String = ""
    var mediaArtUri: Uri = Uri.EMPTY

    var albumId: Long = 0
    var albumTitle: String = ""
    var albumArtist: String = ""
    var albumUri: Uri = Uri.EMPTY

    var insertTime: Long = System.currentTimeMillis()
    var lastPlayTime: Long = System.currentTimeMillis()

    fun toMediaMetaData(): MediaMetadataCompat {
        val metadata = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, this.mediaTitle)
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, this.mediaArtist)
            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, this.albumTitle)
            .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, this.mediaId.toString())
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, this.mediaDuration)
            .putString(MediaMetadataCompat.METADATA_KEY_ART_URI, this.mediaArtUri.toString())
            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, this.albumUri.toString())
            .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, this.mediaUri.toString())
            .putString(MEDIA_MIME_TYPE, this.mediaMimeType)
        return metadata.build()
    }
}