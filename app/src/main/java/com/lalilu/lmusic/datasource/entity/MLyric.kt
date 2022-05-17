package com.lalilu.lmusic.datasource.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "network_lyric")
class MLyric(
    @PrimaryKey
    @ColumnInfo(name = "network_lyric_media_id")
    val mediaId: String,
    @ColumnInfo(name = "network_lyric_lyric")
    val lyric: String,
    @ColumnInfo(name = "network_lyric_tlyric")
    val tlyric: String? = null
)
