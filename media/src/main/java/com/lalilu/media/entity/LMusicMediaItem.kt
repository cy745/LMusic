package com.lalilu.media.entity

import android.media.MediaMetadata
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.lalilu.media.common.GlobalCommon
import com.lalilu.media.common.GlobalCommon.MEDIA_MIME_TYPE
import java.util.*

@Entity(tableName = "lmusic_media_item")
class LMusicMediaItem {
    @PrimaryKey(autoGenerate = true)
    var songId: Long = 0
    var songTitle: String = ""
    var songSize: Long = 0
    var songUri: Uri = Uri.EMPTY
    var songArtist: String = ""
    var songDuration: Long = 0
    var songType: String = ""
    var songArtUri: Uri = Uri.EMPTY

    var albumId: Long = 0
    var albumTitle: String = ""
    var albumArtist: String = ""
    var albumUri: Uri = Uri.EMPTY

    var insertTime: Long = System.currentTimeMillis()
    var lastPlayTime: Long = System.currentTimeMillis()

    fun getInsertTimeText(): String = Date(insertTime).toLocaleString()
    fun getLastPlayTimeText(): String = Date(lastPlayTime).toLocaleString()
    fun getDurationText(): String = GlobalCommon.DurationToString(songDuration)

    fun toMediaItem(): MediaBrowserCompat.MediaItem {
        val description = MediaDescriptionCompat.Builder()
            .setIconUri(this.songArtUri)
            .setMediaUri(this.songUri)
            .setTitle(this.songTitle)
            .setMediaId(this.songId.toString())
            .setSubtitle(this.songArtist)
            .setDescription(this.songArtist)
            .setSubtitle(this.albumTitle)
            .setExtras(Bundle().also {
                it.putString(MediaMetadata.METADATA_KEY_TITLE, this.songTitle)
                it.putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, this.songUri.toString())
                it.putString(MediaMetadata.METADATA_KEY_MEDIA_ID, this.songId.toString())
                it.putString(MediaMetadata.METADATA_KEY_ARTIST, this.songArtist)
                it.putString(MediaMetadata.METADATA_KEY_ART_URI, this.songArtUri.toString())
                it.putString(MediaMetadata.METADATA_KEY_ALBUM, this.albumTitle)
                it.putLong(MediaMetadata.METADATA_KEY_DURATION, this.songDuration)
                it.putString(MEDIA_MIME_TYPE, this.songType)
            }).build()
        return MediaBrowserCompat.MediaItem(description, MediaBrowserCompat.MediaItem.FLAG_PLAYABLE)
    }

    override fun equals(other: Any?): Boolean {
        if (other is LMusicMediaItem) {
            if (other.songId != songId) return false
            if (other.songTitle != songTitle) return false
            if (other.songArtist != songArtist) return false
            if (other.songDuration != songDuration) return false
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
        result = 31 * result + songArtist.hashCode()
        result = 31 * result + songDuration.hashCode()
        result = 31 * result + songType.hashCode()
        result = 31 * result + songArtUri.hashCode()
        result = 31 * result + albumId.hashCode()
        result = 31 * result + albumTitle.hashCode()
        result = 31 * result + albumArtist.hashCode()
        result = 31 * result + albumUri.hashCode()
        result = 31 * result + insertTime.hashCode()
        result = 31 * result + lastPlayTime.hashCode()
        return result
    }
}