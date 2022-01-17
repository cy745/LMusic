package com.lalilu.lmusic.domain.entity

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "m_album")
data class MAlbum(
    @PrimaryKey
    @ColumnInfo(name = "album_id")
    val albumId: Long,

    @ColumnInfo(name = "album_title")
    val albumTitle: String,

    @ColumnInfo(name = "album_cover_uri")
    val albumCoverUri: Uri = Uri.EMPTY,
)