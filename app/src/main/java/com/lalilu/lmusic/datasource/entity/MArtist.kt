package com.lalilu.lmusic.datasource.entity

import androidx.room.*

@Entity(
    tableName = "m_artist",
    foreignKeys = [
        ForeignKey(
            entity = MArtist::class,
            parentColumns = ["artist_name"],
            childColumns = ["map_to_artist"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(
            value = ["map_to_artist"],
            name = "map_to_artist_index"
        )
    ]
)
data class MArtist(
    @PrimaryKey
    @ColumnInfo(name = "artist_name")
    val artistName: String,

    @ColumnInfo(name = "map_to_artist")
    val mapToArtist: String? = null
)
