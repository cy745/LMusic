package com.lalilu.lmusic.datasource.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import java.util.*

@Entity(
    tableName = "song_in_playlist",
    foreignKeys = [
        ForeignKey(
            entity = MPlaylist::class,
            parentColumns = ["playlist_id"],
            childColumns = ["song_in_playlist_playlist_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    primaryKeys = [
        "song_in_playlist_playlist_id",
        "song_in_playlist_song_id"
    ]
)
data class SongInPlaylist(
    @ColumnInfo(name = "song_in_playlist_playlist_id")
    val playlistId: Long,
    @ColumnInfo(name = "song_in_playlist_song_id")
    val mediaId: String,
    @ColumnInfo(name = "song_in_playlist_create_time")
    val time: Date = Date()
)
