package com.lalilu.media.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(primaryKeys = ["playlistId", "musicId"])
data class PlaylistMusicCrossRef(
    val playlistId: Long,
    @ColumnInfo(index = true)
    val musicId: Long
)

