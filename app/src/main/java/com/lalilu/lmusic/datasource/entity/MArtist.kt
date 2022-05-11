package com.lalilu.lmusic.datasource.entity

import androidx.room.*

@Entity(
    tableName = "m_artist",
    foreignKeys = [
        ForeignKey(
            entity = MArtist::class,
            parentColumns = ["artist_id"],
            childColumns = ["map_to_artist_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(
            value = ["map_to_artist_id"],
            name = "map_to_artist_id_index"
        )
    ]
)
data class MArtist(
    @PrimaryKey
    @ColumnInfo(name = "artist_id")
    val artistId: Long,

    @ColumnInfo(name = "artist_name")
    val artistName: String,

    @ColumnInfo(name = "map_to_artist_id")
    val mapToArtist: Long
)
