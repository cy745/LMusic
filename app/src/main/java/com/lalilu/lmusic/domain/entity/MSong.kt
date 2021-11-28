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
)
