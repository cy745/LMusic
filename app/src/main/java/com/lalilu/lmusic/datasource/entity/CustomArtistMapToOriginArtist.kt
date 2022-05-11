package com.lalilu.lmusic.datasource.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "custom_artist_map_to_origin_artist",
    foreignKeys = [
        ForeignKey(
            entity = MArtist::class,
            parentColumns = ["artist_id"],
            childColumns = ["artist_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    primaryKeys = [
        "artist_id",
        "origin_artist_id"
    ]
)
data class CustomArtistMapToOriginArtist(
    @ColumnInfo(name = "artist_id")
    val artistId: Long,
    @ColumnInfo(name = "origin_artist_id")
    val originArtistId: String
)