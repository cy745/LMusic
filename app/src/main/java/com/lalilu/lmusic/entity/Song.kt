package com.lalilu.lmusic.entity

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.lalilu.lmusic.utils.DurationUtils.Companion.durationToString
import java.util.*

@Entity(tableName = "song")
class Song {
    @PrimaryKey(autoGenerate = true)
    var songId: Long = 0
    var songTitle: String = ""
    var songSize: Long = 0
    var songUri: Uri = Uri.EMPTY
    var artist: String = ""

    var duration: Long = 0

    var albumId: Long = 0
    var albumTitle: String = ""
    var albumArtist: String = ""
    var albumUri: Uri = Uri.EMPTY

    var insertTime: Long = System.currentTimeMillis()
    var lastPlayTime: Long = System.currentTimeMillis()

    fun getInsertTimeText(): String = Date(insertTime).toLocaleString()
    fun getLastPlayTimeText(): String = Date(lastPlayTime).toLocaleString()
    fun getDurationText(): String = durationToString(duration)

    override fun equals(other: Any?): Boolean {
        if (other is Song) {
            if (other.songId != songId) return false
            if (other.songTitle != songTitle) return false
            if (other.artist != artist) return false
            if (other.duration != duration) return false
            if (other.albumId != albumId) return false
            if (other.albumTitle != albumTitle) return false
            if (other.insertTime != insertTime) return false
            return true
        }
        return false
    }


    override fun hashCode(): Int {
        var result = songId.hashCode()
        result = 31 * result + songTitle.hashCode()
        result = 31 * result + songSize.hashCode()
        result = 31 * result + songUri.hashCode()
        result = 31 * result + artist.hashCode()
        result = 31 * result + duration.hashCode()
        result = 31 * result + albumId.hashCode()
        result = 31 * result + albumTitle.hashCode()
        result = 31 * result + albumArtist.hashCode()
        result = 31 * result + albumUri.hashCode()
        result = 31 * result + insertTime.hashCode()
        result = 31 * result + lastPlayTime.hashCode()
        return result
    }

    override fun toString(): String {
        return "Song(" +
                "songId=$songId, " + "\n" +
                "songTitle='$songTitle', " + "\n" +
                "songSize=$songSize, " + "\n" +
                "songUri=$songUri, " + "\n" +
                "artist='$artist', " + "\n" +
                "duration=$duration, " + "\n" +
                "albumId=$albumId, " + "\n" +
                "albumTitle='$albumTitle', " + "\n" +
                "albumArtist='$albumArtist', " + "\n" +
                "albumUri=$albumUri, " + "\n" +
                "insertTime=$insertTime, " + "\n" +
                "lastPlayTime=$lastPlayTime" +
                ")"
    }
}