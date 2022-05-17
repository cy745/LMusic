package com.lalilu.lmusic.datasource.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "m_artist_map_id",
    foreignKeys = [
        ForeignKey(
            entity = MArtist::class,
            parentColumns = ["artist_name"],
            childColumns = ["artist_name"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    primaryKeys = [
        "artist_name",
        "origin_artist_id"
    ]
)
data class MArtistMapId(
    @ColumnInfo(name = "artist_name")
    val artistName: String,
    @ColumnInfo(name = "origin_artist_id")
    val originArtistId: String
)