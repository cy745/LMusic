package com.lalilu.media.entity

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "lmusic_playlist")
class LMusicPlayList {
    @PrimaryKey(autoGenerate = true)
    var playListId: Long = 0
    var playListTitle: String = ""
    var playListArtUri: Uri? = Uri.EMPTY

    var insertTime: Long = System.currentTimeMillis()
    var lastPlayTime: Long = System.currentTimeMillis()

    var mediaIdList: TreeSet<String> = TreeSet()
}