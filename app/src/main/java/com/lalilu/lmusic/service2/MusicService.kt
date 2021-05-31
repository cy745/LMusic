package com.lalilu.lmusic.service2

import android.app.PendingIntent
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.ArrayMap
import android.view.KeyEvent
import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaButtonReceiver
import com.lalilu.lmusic.LMusicAudioManager
import com.lalilu.lmusic.database.MusicDatabase
import com.lalilu.lmusic.notification.sendPlayerNotification
import com.lalilu.lmusic.utils.toMediaMeta
import java.util.*

class MusicService : MediaBrowserServiceCompat() {
    private val tag = MusicService::class.java.name

    companion object {
        const val Access_ID = "access_id"
        const val Empty_ID = "empty_id"
        const val Song_Type = "song_type"
    }

    private var prepared = false
    private lateinit var metadataCompat: MediaMetadataCompat
    private lateinit var playbackState: PlaybackStateCompat
    lateinit var musicPlayer: MediaPlayer
    private lateinit var mediaSession: MediaSessionCompat
    lateinit var musicSessionCallback: MusicSessionCallback
    private val resultList: ArrayMap<String, MediaBrowserCompat.MediaItem> = ArrayMap()
    private lateinit var lMusicAudioManager: LMusicAudioManager

    override fun onCreate() {
        super.onCreate()
        musicSessionCallback = MusicSessionCallback()
        lMusicAudioManager = LMusicAudioManager(this)
        musicPlayer = MediaPlayer().also {
            it.setOnPreparedListener(musicSessionCallback)
            it.setOnCompletionListener(musicSessionCallback)
        }

        // 构造可跳转到 launcher activity 的 PendingIntent
        val sessionActivityPendingIntent =
            packageManager?.getLaunchIntentForPackage(packageName)?.let { sessionIntent ->
                PendingIntent.getActivity(this, 0, sessionIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            }

        playbackState = PlaybackStateCompat.Builder()
            .setState(PlaybackStateCompat.STATE_NONE, 0, 1.0f)
            .setActions(
                PlaybackStateCompat.ACTION_PLAY or
                        PlaybackStateCompat.ACTION_PLAY_PAUSE or
                        PlaybackStateCompat.ACTION_PAUSE
            ).build()

        mediaSession = MediaSessionCompat(this, tag).apply {
            setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
            )
            setSessionActivity(sessionActivityPendingIntent)
            setPlaybackState(playbackState)
            setCallback(musicSessionCallback)
            setSessionToken(sessionToken)
        }
    }

    override fun onDestroy() {
        musicSessionCallback.onStop()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        MediaButtonReceiver.handleIntent(mediaSession, intent)
        return super.onStartCommand(intent, flags, startId)
    }

    inner class MusicSessionCallback : MediaSessionCompat.Callback(),
        MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {

        override fun onMediaButtonEvent(mediaButtonEvent: Intent): Boolean {
            if (Intent.ACTION_MEDIA_BUTTON == mediaButtonEvent.action) {
                val event = mediaButtonEvent.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT)
                if (event != null) {
                    val action = event.action
                    val keyCode = event.keyCode
                    if (action == KeyEvent.ACTION_DOWN) {
                        when (keyCode) {
                            KeyEvent.KEYCODE_MEDIA_PLAY -> onPlay()
                            KeyEvent.KEYCODE_MEDIA_PAUSE -> onPause()
                            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> onPlayPause()
                            KeyEvent.KEYCODE_MEDIA_NEXT -> onSkipToNext()
                            KeyEvent.KEYCODE_MEDIA_PREVIOUS -> onSkipToPrevious()
                        }
                    }
                }
            }
            println("[onMediaButtonEvent]: ${mediaButtonEvent.action.toString()}")
            return super.onMediaButtonEvent(mediaButtonEvent)
        }

        private fun onPlayPause() {
            if (playbackState.state == PlaybackStateCompat.STATE_PLAYING) {
                onPause()
            } else {
                onPlay()
            }
        }

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
            val result = lMusicAudioManager.getAudioFocus()
            if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) return

            startService(Intent(this@MusicService, MusicService::class.java))
            mediaSession.isActive = true
            lMusicAudioManager.fadeStart()
            notifyPlayStateChange(PlaybackStateCompat.STATE_PLAYING)
        }

        override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
            if (playbackState.state == PlaybackStateCompat.STATE_NONE
                || playbackState.state == PlaybackStateCompat.STATE_PAUSED
                || playbackState.state == PlaybackStateCompat.STATE_PLAYING
            ) {
                musicPlayer.reset()
                val mediaItem = resultList[mediaId] ?: return
                println(
                    "[onPlayFromMediaId]: [${mediaItem.description.mediaId}]" +
                            " ${mediaItem.description.title}"
                )
                musicPlayer.setDataSource(
                    this@MusicService, mediaItem.description.mediaUri ?: return
                )
                notifyPlayStateChange(PlaybackStateCompat.STATE_BUFFERING)
                notifyMetaDateChange(mediaItem.description.extras!!.toMediaMeta())
                onPrepare()
            }
        }

        override fun onPause() {
            if (playbackState.state == PlaybackStateCompat.STATE_PLAYING) {
                lMusicAudioManager.fadePause()
                notifyPlayStateChange(PlaybackStateCompat.STATE_PAUSED)
                this@MusicService.stopForeground(false)
            }
        }

        override fun onSkipToNext() {
            val now = metadataCompat.description.mediaId
            var index = resultList.indexOfKey(now)
            index = if (index + 1 >= resultList.size) 0 else index + 1
            onPlayFromMediaId(resultList.keyAt(index), null)
        }

        override fun onCustomAction(action: String?, extras: Bundle?) {
            if (action == PlaybackStateCompat.ACTION_PLAY_PAUSE.toString()) {
                onPlayPause()
            }
            super.onCustomAction(action, extras)
        }

        override fun onSkipToPrevious() {
            val now = metadataCompat.description.mediaId
            var index = resultList.indexOfKey(now)
            index = if (index - 1 <= 0) resultList.size - 1 else index - 1
            onPlayFromMediaId(resultList.keyAt(index), null)
        }

        override fun onStop() {
            println("[onStop]")
            lMusicAudioManager.abandonAudioFocus()
            stopSelf()
            mediaSession.isActive = false
            musicPlayer.stop()
            prepared = false
        }

        override fun onSeekTo(pos: Long) {
            musicPlayer.seekTo(pos.toInt())
        }
    }

    fun notifyMetaDateChange(metadata: MediaMetadataCompat) {
        metadataCompat = metadata
        mediaSession.setMetadata(metadataCompat)
    }

    fun notifyPlayStateChange(state: Int) {
        playbackState = PlaybackStateCompat.Builder()
            .setState(state, musicPlayer.currentPosition.toLong(), 1.0f).build()
        mediaSession.setPlaybackState(playbackState)
        if (state == PlaybackStateCompat.STATE_PLAYING || state == PlaybackStateCompat.STATE_PAUSED)
            this@MusicService.sendPlayerNotification(mediaSession)
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
        result.detach()
        if (parentId == Empty_ID) return
        val songList = MusicDatabase.getInstance(this).songDao().getAll()
        resultList.clear()
        songList.forEach { resultList[it.songId.toString()] = it.toMediaItem() }
        result.sendResult(resultList.values.toMutableList())
    }
}

