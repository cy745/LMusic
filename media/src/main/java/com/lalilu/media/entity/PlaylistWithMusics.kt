package com.lalilu.media.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class PlaylistWithMusics(
    @Embedded val playlist: PlayList,
    @Relation(
        parentColumn = "playlistId",
        entityColumn = "musicId",
        associateBy = Junction(PlaylistMusicCrossRef::class)
    )
    val musics: List<Music>
)