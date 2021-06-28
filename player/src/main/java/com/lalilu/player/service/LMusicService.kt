package com.lalilu.player.service

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.media.AudioManager
import android.media.MediaMetadata
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.content.ContextCompat
import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaButtonReceiver
import com.lalilu.media.LMusicMediaModule
import com.lalilu.player.LMusicList
import com.lalilu.player.LMusicPlayerModule
import com.lalilu.player.manager.LMusicAudioFocusManager
import com.lalilu.player.manager.LMusicNotificationManager
import com.lalilu.player.manager.MusicNoisyReceiver
import java.util.*

class LMusicService : MediaBrowserServiceCompat() {
    private val tag = this.javaClass.name

    companion object {
        const val ACCESS_ID = "access_id"
        const val ACTION_SWIPED_SONG = "action_swiped_song"
        const val ACTION_MOVED_SONG = "action_moved_song"
        const val ACTION_PLAY_PAUSE = "play_and_pause"
        const val NEW_MEDIA_ORDER_LIST = "new_media_order_list"
        const val LAST_PLAYED_SONG = "last_played_song"
        val becomingNoisyFilter =
            IntentFilter().apply { addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY) }
    }

    private lateinit var mList: LMusicList
    private lateinit var mLastPlaySongSP: SharedPreferences
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mSessionCallback: LMusicSessionCompactCallback
    private lateinit var mNotificationManager: LMusicNotificationManager
    private lateinit var mAudioFocusManager: LMusicAudioFocusManager
    private lateinit var mNoisyReceiver: MusicNoisyReceiver

    private var mMetadataPrepared: MediaMetadataCompat? = null

    override fun onCreate() {
        super.onCreate()
        val musicDao = LMusicMediaModule.getInstance(null).database.musicDao()
        mList = LMusicList(musicDao)
        mLastPlaySongSP = getSharedPreferences(LAST_PLAYED_SONG, MODE_PRIVATE)
        mNotificationManager = LMusicNotificationManager(this)
        mNoisyReceiver = MusicNoisyReceiver()
        mAudioFocusManager = LMusicAudioFocusManager(this)
        mSessionCallback = LMusicSessionCompactCallback()
        mNoisyReceiver.onBecomingNoisyListener = mSessionCallback
        mAudioFocusManager.onAudioFocusChangeListener = mSessionCallback

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
            setCallback(mSessionCallback)
            setSessionToken(sessionToken)
        }
    }

    inner class LMusicSessionCompactCallback : MediaSessionCompat.Callback(),
        Playback.OnPlayerCallback, AudioManager.OnAudioFocusChangeListener,
        MusicNoisyReceiver.OnBecomingNoisyListener {
        private val playBack: LMusicPlayback =
            LMusicPlayback(this@LMusicService, mList)
                .setOnPlayerCallback(this)
                .setAudioFocusManager(mAudioFocusManager)

        override fun onCustomAction(action: String?, extras: Bundle?) {
            when (action) {
                ACTION_PLAY_PAUSE -> playBack.playAndPause()
                ACTION_SWIPED_SONG -> {
                    val mediaId = extras?.getString(MediaMetadata.METADATA_KEY_MEDIA_ID) ?: return
                    if (mediaId == mList.getNowMediaId()) onSkipToNext()
                    mList.removeMediaId(mediaId)
                }
                ACTION_MOVED_SONG -> {
                    val mediaList = extras?.getStringArrayList(NEW_MEDIA_ORDER_LIST) ?: return
                    mList.updateOrderByNewList(mediaList)
                    LMusicPlayerModule.getInstance(application).mediaList.postValue(
                        mList.getOrderAndShowDataList().toMutableList()
                    )
                }
            }
        }

        override fun onAudioFocusChange(focusChange: Int) {
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                playBack.pause()
            }
        }

        override fun onBecomingNoisy() {
            playBack.pause()
        }

        override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
            playBack.playByMediaId(mediaId)
        }

        override fun onPause() {
            playBack.pause()
        }

        override fun onPlay() {
            playBack.play()
        }

        override fun onSkipToNext() {
            playBack.next()
        }

        override fun onSkipToPrevious() {
            playBack.previous()
        }

        override fun onSeekTo(pos: Long) {
            playBack.seekTo(pos)
        }

        override fun onStop() {
            mediaSession.isActive = false
            unregisterReceiver(mNoisyReceiver)
            playBack.stop()
        }

        override fun onPlaybackStateChanged(newState: Int) {
            val state = PlaybackStateCompat.Builder()
                .setActions(
                    PlaybackStateCompat.ACTION_PLAY or
                            PlaybackStateCompat.ACTION_PLAY_PAUSE or
                            PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID or
                            PlaybackStateCompat.ACTION_PAUSE or
                            PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                ).setState(newState, playBack.getPosition(), 1.0f).build()
            mediaSession.setPlaybackState(state)
            val notification = mNotificationManager.getNotification(mediaSession)
            when (state.state) {
                PlaybackStateCompat.STATE_PLAYING -> {
                    val intent = Intent(this@LMusicService, LMusicService::class.java)
                    ContextCompat.startForegroundService(this@LMusicService, intent)
                    startForeground(LMusicNotificationManager.NOTIFICATION_ID, notification)
                }
                PlaybackStateCompat.STATE_PAUSED -> {
                    stopForeground(false)
                    mNotificationManager.notificationManager.notify(
                        LMusicNotificationManager.NOTIFICATION_ID,
                        notification
                    )
                }
                PlaybackStateCompat.STATE_STOPPED -> {
                    stopForeground(true)
                    stopSelf()
                }
                else -> return
            }
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            if (!mediaSession.isActive) {
                registerReceiver(mNoisyReceiver, becomingNoisyFilter)
            }
            mMetadataPrepared = metadata
            mediaSession.setMetadata(mMetadataPrepared)
            mLastPlaySongSP.edit().putString(
                "mediaId", mMetadataPrepared?.description?.mediaId
            ).apply()
            mediaSession.isActive = true
            LMusicPlayerModule.getInstance(application).mediaList.postValue(
                mList.getOrderAndShowDataList().toMutableList()
            )
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
        val musicDao = LMusicMediaModule.getInstance(null).database.musicDao()
        musicDao.getAllId()?.map {
            mList.setDataIn(it.toString())
        }

        result.sendResult(mutableListOf())
        if (mMetadataPrepared == null) {
            val mediaId = mLastPlaySongSP.getString("mediaId", null) ?: return
            val metadata = mList.getMetadataByMediaId(mediaId) ?: return
            mList.playByMediaId(mediaId) ?: return
            mMetadataPrepared = metadata
            mediaSession.setMetadata(metadata)
        }
        LMusicPlayerModule.getInstance(application).mediaList.postValue(
            mList.getOrderAndShowDataList().toMutableList()
        )
    }
}