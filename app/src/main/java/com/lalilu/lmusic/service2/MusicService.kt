package com.lalilu.lmusic.service2

import android.app.PendingIntent
import android.content.Intent
import android.media.AudioManager
import android.media.MediaMetadata
import android.media.MediaPlayer
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.KeyEvent
import androidx.core.content.ContextCompat
import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaButtonReceiver
import com.lalilu.lmusic.LMusicAudioManager
import com.lalilu.lmusic.LMusicList
import com.lalilu.lmusic.database.MusicDatabase
import com.lalilu.lmusic.entity.Song
import com.lalilu.lmusic.notification.LMusicNotificationManager
import com.lalilu.lmusic.notification.LMusicNotificationManager.Companion.NOTIFICATION_ID
import com.lalilu.lmusic.utils.toMediaMeta
import java.util.*

class MusicService : MediaBrowserServiceCompat() {
    private val tag = MusicService::class.java.name

    companion object {
        const val ACCESS_ID = "access_id"
        const val EMPTY_ID = "empty_id"
        const val SONG_TYPE = "song_type"
        const val ACTION_SWIPED_SONG = "action_swiped_song"
        const val ACTION_MOVE_SONG = "action_swiped_song"
    }

    var musicPlayer: MediaPlayer? = null
    lateinit var musicSessionCallback: MusicSessionCallback

    private var nowMetadata: MediaMetadataCompat? = null
    private lateinit var nowPlaybackState: PlaybackStateCompat

    private lateinit var mNotificationManager: LMusicNotificationManager
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var lMusicAudioManager: LMusicAudioManager

    private val mList: LMusicList<String, MediaBrowserCompat.MediaItem> = LMusicList()
    private var isInForeGroundState = false

    fun recreatePlayer() {
        musicPlayer = MediaPlayer().also {
            it.setOnPreparedListener(musicSessionCallback)
            it.setOnCompletionListener(musicSessionCallback)
        }
    }

    override fun onCreate() {
        super.onCreate()
        mNotificationManager = LMusicNotificationManager(this)
        musicSessionCallback = MusicSessionCallback()
        lMusicAudioManager = LMusicAudioManager(this)
        recreatePlayer()

        // 构造可跳转到 launcher activity 的 PendingIntent
        val sessionActivityPendingIntent =
            packageManager?.getLaunchIntentForPackage(packageName)?.let { sessionIntent ->
                PendingIntent.getActivity(this, 0, sessionIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            }

        nowPlaybackState = PlaybackStateCompat.Builder()
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
            setPlaybackState(nowPlaybackState)
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
            if (mediaButtonEvent.action != Intent.ACTION_MEDIA_BUTTON)
                return super.onMediaButtonEvent(mediaButtonEvent)
            val event = mediaButtonEvent.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT)
                ?: return super.onMediaButtonEvent(mediaButtonEvent)
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
            println("[onMediaButtonEvent]: ${mediaButtonEvent.action.toString()}")
            return super.onMediaButtonEvent(mediaButtonEvent)
        }

        private fun onPlayPause() {
            when (nowPlaybackState.state) {
                PlaybackStateCompat.STATE_PLAYING -> onPause()
                PlaybackStateCompat.STATE_PAUSED -> onPlay()
                else -> println("[onPlayPause]: ${nowPlaybackState.state}")
            }
        }

        override fun onCompletion(mp: MediaPlayer?) {
            onSkipToNext()
        }

        override fun onPrepare() {
            musicPlayer?.prepare()
        }

        override fun onPrepared(mp: MediaPlayer?) = onPlay()

        override fun onPlay() {
            val result = lMusicAudioManager.getAudioFocus()
            if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) return

            mediaSession.isActive = true
            lMusicAudioManager.fadeStart()
            notifyPlayStateChange(PlaybackStateCompat.STATE_PLAYING)
        }

        override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
//            println("[onPlayFromMediaId]: $mediaId")
            if (nowPlaybackState.state == PlaybackStateCompat.STATE_NONE
                || nowPlaybackState.state == PlaybackStateCompat.STATE_PAUSED
                || nowPlaybackState.state == PlaybackStateCompat.STATE_PLAYING
            ) {
                if (musicPlayer == null) recreatePlayer()
                musicPlayer?.reset()
                val mediaItem = mList.playByKey(mediaId) ?: return
                val mediaUri = mediaItem.description.mediaUri ?: return
                val metadata = mediaItem.description.extras!!.toMediaMeta()
                musicPlayer?.setDataSource(this@MusicService, mediaUri)
                notifyPlayStateChange(PlaybackStateCompat.STATE_BUFFERING)
                notifyMetaDateChange(metadata)
                onPrepare()
            }
        }

        override fun onPause() {
            if (nowPlaybackState.state == PlaybackStateCompat.STATE_PLAYING) {
                lMusicAudioManager.fadePause()
                notifyPlayStateChange(PlaybackStateCompat.STATE_PAUSED)
            }
        }

        override fun onSkipToPrevious() {
            val previous = mList.last() ?: return
            onPlayFromMediaId(previous.mediaId, null)
        }

        override fun onSkipToNext() {
            val next = mList.next() ?: return
            onPlayFromMediaId(next.mediaId, null)
        }

        override fun onCustomAction(action: String?, extras: Bundle?) {
            when (action) {
                PlaybackStateCompat.ACTION_PLAY_PAUSE.toString() -> {
                    onPlayPause()
                }
                ACTION_SWIPED_SONG -> {
                    val mediaId = extras?.get(MediaMetadata.METADATA_KEY_MEDIA_ID) ?: return
                    mList.mOrderList.remove(mediaId)
                }
            }
//            super.onCustomAction(action, extras)
        }

        override fun onStop() {
            mediaSession.isActive = false
            lMusicAudioManager.abandonAudioFocus()
            if (musicPlayer != null) {
                musicPlayer!!.reset()
                musicPlayer!!.release()
                musicPlayer = null
            }
            notifyPlayStateChange(PlaybackStateCompat.STATE_STOPPED)
        }

        override fun onSeekTo(pos: Long) {
            musicPlayer?.seekTo(pos.toInt())
            notifyPlayStateChange(PlaybackStateCompat.STATE_PLAYING)
        }
    }

    fun notifyMetaDateChange(metadata: MediaMetadataCompat) {
        nowMetadata = metadata
        mediaSession.setMetadata(nowMetadata)
    }

    fun notifyPlayStateChange(state: Int) {
        nowPlaybackState = PlaybackStateCompat.Builder()
            .setState(
                state,
                if (musicPlayer == null) 0 else musicPlayer!!.currentPosition.toLong(),
                1.0f
            ).build()
        mediaSession.setPlaybackState(nowPlaybackState)

        when (state) {
            PlaybackStateCompat.STATE_PLAYING -> {
                val notification = mNotificationManager.getNotification(mediaSession)
                if (!isInForeGroundState) {
                    ContextCompat.startForegroundService(
                        this@MusicService,
                        Intent(this@MusicService, MusicService::class.java)
                    )
                    isInForeGroundState = true
                }
                startForeground(NOTIFICATION_ID, notification)
            }
            PlaybackStateCompat.STATE_PAUSED -> {
                stopForeground(false)
                val notification = mNotificationManager.getNotification(mediaSession)
                mNotificationManager.notificationManager.notify(NOTIFICATION_ID, notification)
            }
            PlaybackStateCompat.STATE_STOPPED -> {
                stopForeground(true)
                stopSelf()
                isInForeGroundState = false
            }
        }
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot {
        return BrowserRoot(ACCESS_ID, null)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        result.detach()
        if (parentId == EMPTY_ID) return
        val songList = MusicDatabase.getInstance(this).songDao().getAll()

        for (song: Song in songList) {
            val mediaItem = song.toMediaItem()
            mList.setValueIn(mediaItem.mediaId.toString(), mediaItem)
        }
        result.sendResult(mList.getOrderDataList().toMutableList())
    }
}

