package com.lalilu.lmusic.entity

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadata
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import androidx.core.net.toFile
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.lalilu.lmusic.service2.MusicService
import com.lalilu.lmusic.utils.DurationUtils.Companion.durationToString
import java.util.*

@Entity(tableName = "song")
class Song {
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
    fun getDurationText(): String = durationToString(songDuration)

    fun toMediaItem(): MediaBrowserCompat.MediaItem {
        val description = MediaDescriptionCompat.Builder()
            .setIconBitmap(loadBitmapFromUri(songArtUri, 400))
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
                it.putString(MusicService.Song_Type, this.songType)
            }).build()
        return MediaBrowserCompat.MediaItem(
            description,
            MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
        )
    }

    private fun loadBitmapFromUri(uri: Uri, toSize: Int): Bitmap? {
        val outOption = BitmapFactory.Options().also {
            it.inJustDecodeBounds = true
        }
        BitmapFactory.decodeStream(uri.toFile().inputStream(), null, outOption)

        val outWidth = outOption.outWidth
        val outHeight = outOption.outHeight
        if (outWidth == -1 || outHeight == -1) return null

        var scaleValue: Int = if (outWidth > toSize) outWidth / toSize else toSize / outWidth
        if (scaleValue < 1) scaleValue = 1

        outOption.also {
            it.inJustDecodeBounds = false
            it.inSampleSize = scaleValue
        }

        return BitmapFactory.decodeStream(uri.toFile().inputStream(), null, outOption)
    }

    override fun equals(other: Any?): Boolean {
        if (other is Song) {
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
                "songArtist='$songArtist', " + "\n" +
                "songDuration=$songDuration, " + "\n" +
                "songType=$songType, " + "\n" +
                "albumId=$albumId, " + "\n" +
                "albumTitle='$albumTitle', " + "\n" +
                "albumArtist='$albumArtist', " + "\n" +
                "albumUri=$albumUri, " + "\n" +
                "insertTime=$insertTime, " + "\n" +
                "lastPlayTime=$lastPlayTime" +
                ")"
    }
}