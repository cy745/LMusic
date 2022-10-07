package com.lalilu.lmusic.datasource.entity

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "m_play_history")
data class PlayHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val mediaId: String,
    val startTime: Long
)