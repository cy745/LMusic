package com.lalilu.lmusic.domain.entity

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "m_song_detail")
data class MSongDetail(
    @PrimaryKey
    @ColumnInfo(name = "song_id")
    val songId: Long,

    @ColumnInfo(name = "song_lyric")
    val songLyric: String = "",

    @ColumnInfo(name = "song_size")
    val songSize: Long = 0,

    @ColumnInfo(name = "song_cover_uri")
    var songCoverUri: Uri = Uri.EMPTY,

    @ColumnInfo(name = "song_data")
    val songData: String = ""
)