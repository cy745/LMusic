package com.lalilu.lmusic.domain.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "m_playlist")
data class MPlaylist(

    @PrimaryKey
    @ColumnInfo(name = "playlist_id")
    val playlistId: Long,

    @ColumnInfo(name = "playlist_info")
    val playlistInfo: String = "empty.",
)