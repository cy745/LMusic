package com.lalilu.lhistory.entity

import androidx.annotation.IntDef
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

const val HISTORY_TYPE_SONG = 0
const val HISTORY_TYPE_ALBUM = 1
const val HISTORY_TYPE_PLAYLIST = 2
const val HISTORY_TYPE_ARTIST = 3

@IntDef(HISTORY_TYPE_SONG, HISTORY_TYPE_ALBUM, HISTORY_TYPE_PLAYLIST, HISTORY_TYPE_ARTIST)
@Retention(AnnotationRetention.SOURCE)
annotation class HistoryType

@Entity(tableName = "m_history")
data class LHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val contentId: String,

    // 数据库层面0为正常值，而-1代表预保存记录，即会被清除的记录
    @ColumnInfo(name = "duration", defaultValue = "0")
    val duration: Long = -1L,
    val startTime: Long = System.currentTimeMillis(),

    @HistoryType
    val type: Int
)

