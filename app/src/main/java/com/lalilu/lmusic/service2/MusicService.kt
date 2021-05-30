package com.lalilu.lmusic.service2

import android.app.PendingIntent
import android.media.MediaMetadata
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.media.MediaBrowserServiceCompat
import com.lalilu.lmusic.database.MusicDatabase
import com.lalilu.lmusic.entity.Song
import java.util.*
import kotlin.collections.LinkedHashMap

class MusicService : MediaBrowserServiceCompat() {
    companion object {
        const val Access_ID = "access_id"
        const val Empty_ID = "empty_id"
        const val Tag_Name = "MusicService"
        const val Song_Type = "song_type"
    }

    private var prepared = false
    private lateinit var metadataCompat: MediaMetadataCompat
    private lateinit var playbackState: PlaybackStateCompat
    private lateinit var musicPlayer: MediaPlayer
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var musicSessionCallback: MusicSessionCallback
    private val resultList: LinkedHashMap<String, MediaBrowserCompat.MediaItem> = LinkedHashMap()

    override fun onCreate() {
        super.onCreate()

        musicSessionCallback = MusicSessionCallback()
        musicPlayer = MediaPlayer().also {
            it.setOnPreparedListener(musicSessionCallback)
            it.setOnCompletionListener(musicSessionCallback)
        }

        // 构造可跳转到 launcher activity 的 PendingIntent
        val sessionActivityPendingIntent =
            packageManager?.getLaunchIntentForPackage(packageName)?.let { sessionIntent ->
                PendingIntent.getActivity(this, 0, sessionIntent, 0)
            }

        playbackState = PlaybackStateCompat.Builder()
            .setState(PlaybackStateCompat.STATE_NONE, 0, 1.0f)
            .setActions(
                PlaybackStateCompat.ACTION_PLAY or
                        PlaybackStateCompat.ACTION_PLAY_PAUSE or
                        PlaybackStateCompat.ACTION_PAUSE
            ).build()

        mediaSession = MediaSessionCompat(this, Tag_Name).apply {
            setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
            )
            setSessionActivity(sessionActivityPendingIntent)
            setPlaybackState(playbackState)
            setCallback(musicSessionCallback)
            setSessionToken(sessionToken)
            isActive = true
        }
    }

    inner class MusicSessionCallback : MediaSessionCompat.Callback(),
        MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {

        override fun onPrepared(mp: MediaPlayer?) {
            println("[MusicSessionCallback]#onPrepared")
            prepared = true
            onPlay()
        }

        override fun onCompletion(mp: MediaPlayer?) {
            println("[MusicSessionCallback]#onCompletion")
            onSkipToNext()
        }

        override fun onPrepare() {
            musicPlayer.prepare()
        }

        override fun onPlay() {
            if (prepared) {
                musicPlayer.start()
                playbackState = PlaybackStateCompat.Builder()
                    .setState(PlaybackStateCompat.STATE_PLAYING, 0, 1.0f).build()
                mediaSession.setPlaybackState(playbackState)
            }
        }

        override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
            when (playbackState.state) {
                PlaybackStateCompat.STATE_NONE,
                PlaybackStateCompat.STATE_PAUSED,
                PlaybackStateCompat.STATE_PLAYING -> {
                    val metadata = resultList[mediaId] ?: return
                    println("[MusicService]#onPlayFromMediaId: [${metadata.description.mediaId}] ${metadata.description.title}")
                    musicPlayer.reset()
                    musicPlayer.setDataSource(
                        this@MusicService,
                        metadata.description.mediaUri ?: return
                    )
                    playbackState = PlaybackStateCompat.Builder()
                        .setState(PlaybackStateCompat.STATE_BUFFERING, 0, 1.0f)
                        .build()
                    metadataCompat = mediaItemExtraToMeta(metadata.description.extras)
                    mediaSession.setPlaybackState(playbackState)
                    mediaSession.setMetadata(metadataCompat)
                    onPrepare()
                }
                else -> {
                    println("[onPlayFromMediaId]: no state compare.")
                }
            }
        }

        override fun onPlayFromUri(uri: Uri?, extras: Bundle?) {
            if (playbackState.state ==
                PlaybackStateCompat.STATE_NONE or
                PlaybackStateCompat.STATE_PAUSED or
                PlaybackStateCompat.STATE_PLAYING
            ) {
                musicPlayer.reset()
                musicPlayer.setDataSource(this@MusicService, uri ?: return)
                playbackState = PlaybackStateCompat.Builder()
                    .setState(PlaybackStateCompat.STATE_CONNECTING, 0, 1.0f)
                    .build()
                metadataCompat = mediaItemExtraToMeta(extras)
                mediaSession.setPlaybackState(playbackState)
                mediaSession.setMetadata(metadataCompat)
                onPrepare()
            }
        }

        override fun onPause() {
            if (playbackState.state == PlaybackStateCompat.STATE_PLAYING) {
                musicPlayer.pause()
                playbackState = PlaybackStateCompat.Builder()
                    .setState(PlaybackStateCompat.STATE_PAUSED, 0, 1.0f).build()
                mediaSession.setPlaybackState(playbackState)
            }
        }

        override fun onSkipToNext() {
            val now = metadataCompat.description.mediaId
            val iterator = resultList.keys.iterator()
            while (iterator.hasNext()) {
                if (iterator.next() == now) {
                    onPlayFromMediaId(iterator.next(), null)
                    break
                }
            }
        }

        override fun onStop() {
            musicPlayer.stop()
            prepared = false
        }

        override fun onSeekTo(pos: Long) {
            musicPlayer.seekTo(pos.toInt())
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
        resultList.clear()
        for (song: Song in songList) {
            resultList[song.songId.toString()] = song.toMediaItem()
        }
        result.sendResult(resultList.values.toMutableList())
    }

    private fun mediaItemExtraToMeta(extras: Bundle?): MediaMetadataCompat {
        if (extras == null) return MediaMetadataCompat.Builder().build()
        try {
            return MediaMetadataCompat.Builder()
                .putString(
                    MediaMetadata.METADATA_KEY_TITLE,
                    extras.getString(MediaMetadata.METADATA_KEY_TITLE)
                )
                .putString(
                    MediaMetadataCompat.METADATA_KEY_MEDIA_URI,
                    extras.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI)
                )
                .putString(
                    MediaMetadata.METADATA_KEY_MEDIA_ID,
                    extras.getString(MediaMetadata.METADATA_KEY_MEDIA_ID)
                )
                .putString(
                    MediaMetadata.METADATA_KEY_ARTIST,
                    extras.getString(MediaMetadata.METADATA_KEY_ARTIST)
                )
                .putString(
                    MediaMetadata.METADATA_KEY_ART_URI,
                    extras.getString(MediaMetadata.METADATA_KEY_ART_URI)
                )
                .putString(
                    MediaMetadata.METADATA_KEY_ALBUM,
                    extras.getString(MediaMetadata.METADATA_KEY_ALBUM)
                )
                .putLong(
                    MediaMetadata.METADATA_KEY_DURATION,
                    extras.getLong(MediaMetadata.METADATA_KEY_DURATION)
                )
                .putString(Song_Type, extras.getString(Song_Type))
                .build()
        } catch (e: Exception) {
            e.printStackTrace()
            return MediaMetadataCompat.Builder().build()
        }
    }
}

