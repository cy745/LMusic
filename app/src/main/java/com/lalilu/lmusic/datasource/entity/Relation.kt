package com.lalilu.lmusic.datasource.entity

import androidx.room.Embedded
import androidx.room.Relation

data class ArtistMapIds(
    @Embedded val artist: MArtist,
    @Relation(
        parentColumn = "artist_name",
        entityColumn = "artist_name"
    )
    val mapIds: List<MArtistMapId>
)
