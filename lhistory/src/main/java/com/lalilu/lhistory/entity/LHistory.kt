package com.lalilu.lhistory.entity

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "m_history")
data class LHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val contentId: String,
    val contentTitle: String,
    val parentId: String = "",
    val parentTitle: String = "",

    // 数据库层面0为正常值，而-1代表预保存记录，即会被清除的记录
    val duration: Long = -1L,
    val repeatCount: Int = 0,
    val startTime: Long = System.currentTimeMillis(),
)

