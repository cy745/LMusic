package com.lalilu.lmusic.service2

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.media.AudioManager
import android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY
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
import com.lalilu.lmusic.*
import com.lalilu.lmusic.notification.LMusicNotificationManager
import com.lalilu.lmusic.notification.LMusicNotificationManager.Companion.NOTIFICATION_ID
import com.lalilu.media.LMusicMediaModule
import com.lalilu.media.toMediaItem
import java.util.*
import java.util.logging.Logger


class MusicService : MediaBrowserServiceCompat() {
    private val tag = MusicService::class.java.name

    companion object {
        const val ACCESS_ID = "access_id"
        const val ACTION_SWIPED_SONG = "action_swiped_song"
        const val ACTION_MOVE_SONG = "action_swiped_song"
        const val LAST_PLAYED_SONG = "last_played_song"
    }

    lateinit var lastPlaySongSP: SharedPreferences
    lateinit var musicSessionCallback: MusicSessionCallback
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mAudioFocusManager: LMusicAudioFocusManager
    private lateinit var mNotificationManager: LMusicNotificationManager
    private lateinit var mNoisyReceiver: MusicNoisyReceiver
    private lateinit var mMusicPlayback: MusicPlayback

    private val mList: LMusicList<String, MediaMetadataCompat> = LMusicList()
    private var isInForeGroundState = false
    private var mMetadataPrepared: MediaMetadataCompat? = null

    override fun onCreate() {
        super.onCreate()
        lastPlaySongSP = getSharedPreferences(LAST_PLAYED_SONG, MODE_PRIVATE)
        mNotificationManager = LMusicNotificationManager(this)
        musicSessionCallback = MusicSessionCallback()
        mAudioFocusManager = LMusicAudioFocusManager(this, musicSessionCallback)
        mMusicPlayback = MusicPlayback(musicSessionCallback)
        mNoisyReceiver = MusicNoisyReceiver().also {
            it.onBecomingNoisyCallback = {
                musicSessionCallback.onPause()
            }
        }

        // 构造可跳转到 launcher activity 的 PendingIntent
        val sessionActivityPendingIntent =
            packageManager?.getLaunchIntentForPackage(packageName)?.let { sessionIntent ->
                PendingIntent.getActivity(this, 0, sessionIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            }

        val flags = MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS

        mediaSession = MediaSessionCompat(this, tag).apply {
            setFlags(flags)
            setSessionActivity(sessionActivityPendingIntent)
            setCallback(musicSessionCallback)
            setSessionToken(sessionToken)
        }
    }

    inner class MusicSessionCallback : MediaSessionCompat.Callback(),
        MusicPlayback.MusicPlaybackListener, AudioManager.OnAudioFocusChangeListener {

        private val logger = Logger.getLogger(this.javaClass.name)

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
            logger.info("[onMediaButtonEvent]: ${mediaButtonEvent.action.toString()}")
            return super.onMediaButtonEvent(mediaButtonEvent)
        }

        override fun onAudioFocusChange(focusChange: Int) {
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS) onPause()
        }

        override fun notifyNewPlayBackState(state: PlaybackStateCompat) {
            mediaSession.setPlaybackState(state)
            val notification = mNotificationManager.getNotification(mediaSession)
            when (state.state) {
                PlaybackStateCompat.STATE_PLAYING -> {
                    if (!isInForeGroundState) {
                        val intent = Intent(this@MusicService, MusicService::class.java)
                        ContextCompat.startForegroundService(this@MusicService, intent)
                        isInForeGroundState = true
                    }
                    startForeground(NOTIFICATION_ID, notification)
                }
                PlaybackStateCompat.STATE_PAUSED -> {
                    stopForeground(false)
                    mNotificationManager.notificationManager.notify(NOTIFICATION_ID, notification)
                }
                PlaybackStateCompat.STATE_STOPPED -> {
                    stopForeground(true)
                    stopSelf()
                    isInForeGroundState = false
                }
            }
        }

        override fun onCompletion(mp: MediaPlayer?) = onSkipToNext()
        override fun onSeekTo(pos: Long) = mMusicPlayback.seekTo(pos)
        override fun onPause() = mMusicPlayback.pause()
        private fun onPlayPause() = mMusicPlayback.toggle()

        override fun onPlay() {
            val result = mAudioFocusManager.getAudioFocus()
            if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) return

            val mediaId = mList.getNowItem()?.description?.mediaId ?: return
            onPlayFromMediaId(mediaId, null)
        }

        override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
            logger.info("[onPlayFromMediaId]: $mediaId")

            val mediaMetaData = mList.playByKey(mediaId) ?: return
            val mediaUri = mediaMetaData.description.mediaUri ?: return

            mMusicPlayback.playFromUri(mediaUri, this@MusicService)

            val filter = IntentFilter().also { it.addAction(ACTION_AUDIO_BECOMING_NOISY) }
            registerReceiver(mNoisyReceiver, filter)
            onPrepare()
        }

        override fun onPrepare() {
            if (mMetadataPrepared == mList.getNowItem()) return
            mMetadataPrepared = mList.getNowItem()
            mediaSession.setMetadata(mMetadataPrepared)
            lastPlaySongSP.edit().putString(
                "mediaId", mMetadataPrepared?.description?.mediaId
            ).apply()
            mediaSession.isActive = true
        }

        override fun onSkipToPrevious() {
            val previous = mList.last() ?: return
            onPlayFromMediaId(previous.description.mediaId, null)
        }

        override fun onSkipToNext() {
            val next = mList.next() ?: return
            onPlayFromMediaId(next.description.mediaId, null)
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
        }

        override fun onStop() {
            mediaSession.isActive = false
            mAudioFocusManager.abandonAudioFocus()
            unregisterReceiver(mNoisyReceiver)
            mMusicPlayback.stop()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        MediaButtonReceiver.handleIntent(mediaSession, intent)
        return super.onStartCommand(intent, flags, startId)
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
        val metadataList = LMusicMediaModule.getInstance(null)
            .mediaScanner.getMediaMetaData()

        metadataList.forEach { metadata ->
            metadata.description?.mediaId?.let {
                mList.setValueIn(it, metadata)
            }
        }

        result.sendResult(mList.getOrderDataList().map {
            it.toMediaItem()
        }.toMutableList())

        if (mMetadataPrepared == null) {
            val mediaId = lastPlaySongSP.getString("mediaId", null) ?: return
            val metadata = mList.playByKey(mediaId) ?: return
            mMetadataPrepared = metadata
            mediaSession.setMetadata(metadata)
        }
    }
}

