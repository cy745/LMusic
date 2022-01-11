package com.lalilu.lmusic.domain.entity

import android.net.Uri
import android.support.v4.media.MediaMetadataCompat
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.lalilu.lmusic.Config

@Entity(tableName = "m_song")
data class MSong(
    @PrimaryKey
    @ColumnInfo(name = "song_id")
    val songId: Long,

    @ColumnInfo(name = "album_id")
    val albumId: Long,

    @ColumnInfo(name = "album_title")
    val albumTitle: String,

    @ColumnInfo(name = "song_uri")
    val songUri: Uri,

    @ColumnInfo(name = "song_title")
    val songTitle: String,

    @ColumnInfo(name = "song_duration")
    val songDuration: Long,

    @ColumnInfo(name = "song_cover_uri")
    var songCoverUri: Uri = Uri.EMPTY,

    @ColumnInfo(name = "showing_artist")
    val showingArtist: String = "",

    @ColumnInfo(name = "song_mime_type")
    val songMimeType: String = "",
) {
    fun toMediaMetadataCompat(): MediaMetadataCompat {
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MSong

        if (songId != other.songId) return false

        return true
    }

    override fun hashCode(): Int {
        return songId.hashCode()
    }
}

data class MSongUpdateCoverUri(
    @ColumnInfo(name = "song_id")
    val songId: Long,
    @ColumnInfo(name = "song_cover_uri")
    val songCoverUri: Uri
)