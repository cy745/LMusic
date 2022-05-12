package com.lalilu.lmusic.datasource.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "custom_artist_map_to_origin_artist",
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
data class CustomArtistMapToOriginArtist(
    @ColumnInfo(name = "artist_name")
    val artistName: String,
    @ColumnInfo(name = "origin_artist_id")
    val originArtistId: String
)