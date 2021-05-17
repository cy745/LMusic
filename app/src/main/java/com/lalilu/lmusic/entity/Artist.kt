package com.lalilu.lmusic.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "artist")
class Artist {
    @PrimaryKey(autoGenerate = true)
    var artistId: Long = 0
    var artistName: String = ""
}