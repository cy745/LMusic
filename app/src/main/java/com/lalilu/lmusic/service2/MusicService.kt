package com.lalilu.lmusic.service2

import android.app.PendingIntent
import android.media.MediaMetadata
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.media.MediaBrowserServiceCompat
import com.lalilu.lmusic.MusicApplication
import com.lalilu.lmusic.database.MusicDatabase
import com.lalilu.lmusic.entity.Song
import com.lalilu.lmusic.utils.AudioMediaScanner

class MusicService : MediaBrowserServiceCompat() {
    companion object {
        const val Access_ID = "access_id"
        const val Empty_ID = "empty_id"
        const val SongType = "song_type"
    }

    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var audioMediaScanner: AudioMediaScanner
    private lateinit var musicPlayer: MusicPlayer
    private lateinit var musicSessionCallback: MusicSessionCallback

    override fun onCreate() {
        super.onCreate()
        audioMediaScanner = (application as MusicApplication).audioMediaScanner

        musicPlayer = MusicPlayer(this)
        musicSessionCallback = MusicSessionCallback(musicPlayer)

        // 构造可跳转到 launcher activity 的 PendingIntent
        val sessionActivityPendingIntent =
            packageManager?.getLaunchIntentForPackage(packageName)?.let { sessionIntent ->
                PendingIntent.getActivity(this, 0, sessionIntent, 0)
            }

        mediaSession = MediaSessionCompat(this, "MusicService").apply {
            setSessionActivity(sessionActivityPendingIntent)
            setCallback(musicSessionCallback)
            setSessionToken(sessionToken)
//            setPlaybackState(PlaybackState)
            isActive = true
        }
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot {
        return BrowserRoot(Access_ID, null)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        if (parentId == Empty_ID) return

        val songList = MusicDatabase.getInstance(this).songDao().getAll()
        val resultList: MutableList<MediaBrowserCompat.MediaItem> = ArrayList()

        for (song: Song in songList) {
            val description = MediaDescriptionCompat.Builder()
                .setIconUri(song.songArtUri)
                .setMediaUri(song.songUri)
                .setTitle(song.songTitle)
                .setMediaId(song.songId.toString())
                .setSubtitle(song.songArtist)
                .setExtras(Bundle().also {
                    it.putString(MediaMetadata.METADATA_KEY_ALBUM, song.albumTitle)
                    it.putString(MediaMetadata.METADATA_KEY_ARTIST, song.albumArtist)
                    it.putLong(MediaMetadata.METADATA_KEY_DURATION, song.songDuration)
                    it.putString(
                        MediaMetadata.METADATA_KEY_ART_URI, song.songArtUri.toString()
                    )
                    it.putString(SongType, song.songType)
                }).build()

            resultList.add(
                MediaBrowserCompat.MediaItem(
                    description,
                    MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
                )
            )
        }
        result.sendResult(resultList)
    }
}