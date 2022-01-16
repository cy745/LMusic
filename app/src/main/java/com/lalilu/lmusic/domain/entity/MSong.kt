package com.lalilu.lmusic.domain.entity

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

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

    @ColumnInfo(name = "showing_artist")
    val showingArtist: String = "",

    @ColumnInfo(name = "song_mime_type")
    val songMimeType: String = "",
) {
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
