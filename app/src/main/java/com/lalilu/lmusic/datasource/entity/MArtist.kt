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

    /**
     * 查询所有 [mapToArtist] 为 null 的 [MArtist] 即可只查询到顶级的 [MArtist]
     */
    @ColumnInfo(name = "map_to_artist")
    val mapToArtist: String
)
