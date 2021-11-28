package com.lalilu.lmusic.domain.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "m_artist")
data class MArtist(
    @PrimaryKey
    @ColumnInfo(name = "artist_name")
    val artistName: String
)