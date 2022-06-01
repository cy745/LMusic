package com.lalilu.lmusic.datasource.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "network_data")
data class MNetworkData(
    @PrimaryKey
    @ColumnInfo(name = "network_data_media_id")
    val mediaId: String,
    @ColumnInfo(name = "network_data_song_id")
    val songId: String,
    @ColumnInfo(name = "network_data_title")
    val title: String,
    @ColumnInfo(name = "network_data_cover")
    var cover: String? = null,
    @ColumnInfo(name = "network_data_lyric")
    var lyric: String? = null,
    @ColumnInfo(name = "network_data_tlyric")
    var tlyric: String? = null
)

data class MNetworkDataUpdateForLyric(
    @ColumnInfo(name = "network_data_media_id")
    val mediaId: String,
    @ColumnInfo(name = "network_data_lyric")
    val lyric: String,
    @ColumnInfo(name = "network_data_tlyric")
    val tlyric: String? = null
)
