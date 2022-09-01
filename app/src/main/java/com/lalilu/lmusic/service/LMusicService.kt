package com.lalilu.lmusic.service

import android.app.PendingIntent
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaButtonReceiver
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmusic.Config.MEDIA_DEFAULT_ACTION
import com.lalilu.lmusic.Config.MEDIA_STOPPED_STATE
import com.lalilu.lmusic.repository.HistoryDataStore
import com.lalilu.lmusic.repository.SettingsDataStore
import com.lalilu.lmusic.service.LMusicNotification.Companion.NOTIFICATION_PLAYER_ID
import com.lalilu.lmusic.utils.extension.getNextOf
import com.lalilu.lmusic.utils.extension.getPreviousOf
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

@AndroidEntryPoint
class LMusicService : MediaBrowserServiceCompat(), CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.IO

    @Inject
    lateinit var historyDataStore: HistoryDataStore

    @Inject
    lateinit var settingsDataStore: SettingsDataStore

    @Inject
    lateinit var mNotificationManager: LMusicNotification

    private lateinit var mediaSession: MediaSessionCompat
    private val playBack: LMusicPlayBack<LSong> = object : LMusicPlayBack<LSong>(this) {
        private val noisyReceiver = LMusicNoisyReceiver(this::onPause)
        private val audioFocusHelper = LMusicAudioFocusHelper(this@LMusicService) {
            when (it) {
                AudioManager.AUDIOFOCUS_LOSS,
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> onPause()
            }
        }

        override fun getUriFromItem(item: LSong): Uri = item.uri
        override fun getCurrent(): LSong? = LMusicRuntime.currentPlaying
        override fun getMetaDataFromItem(item: LSong?): MediaMetadataCompat? = item?.metadataCompat

        override fun requestAudioFocus(): Boolean {
            return audioFocusHelper.requestAudioFocus() == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }

        override fun skipToItemByID(id: String) {
            LMusicRuntime.currentPlaying = LMusicRuntime.currentPlaylist.find { it.id == id }
        }

        override fun skipToNext(random: Boolean) {
            settingsDataStore.apply {
                val current = LMusicRuntime.currentPlaying
                LMusicRuntime.currentPlaying = when (repeatModeKey.get()) {
                    PlaybackStateCompat.REPEAT_MODE_INVALID,
                    PlaybackStateCompat.REPEAT_MODE_NONE ->
                        LMusicRuntime.currentPlaylist.getNextOf(current, false)
                    PlaybackStateCompat.REPEAT_MODE_ONE -> current
                    else -> LMusicRuntime.currentPlaylist.getNextOf(current, true)
                }
            }
        }

        override fun skipToPrevious(random: Boolean) {
            settingsDataStore.apply {
                val current = LMusicRuntime.currentPlaying
                LMusicRuntime.currentPlaying = when (repeatModeKey.get()) {
                    PlaybackStateCompat.REPEAT_MODE_INVALID,
                    PlaybackStateCompat.REPEAT_MODE_NONE ->
                        LMusicRuntime.currentPlaylist.getPreviousOf(current, false)
                    PlaybackStateCompat.REPEAT_MODE_ONE -> current
                    else -> LMusicRuntime.currentPlaylist.getPreviousOf(current, true)
                }
            }
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            mediaSession.setMetadata(metadata)
            mediaSession.isActive = true
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == PlaybackStateCompat.STATE_STOPPED) {
                LMusicRuntime.updatePosition(0, false)
                noisyReceiver.unRegisterFrom(this@LMusicService)
                audioFocusHelper.abandonAudioFocus()
                stopForeground()
                mediaSession.setPlaybackState(MEDIA_STOPPED_STATE)
                mediaSession.isActive = false
                stopSelf()
                return
            }

            mediaSession.setPlaybackState(
                PlaybackStateCompat.Builder()
                    .setActions(MEDIA_DEFAULT_ACTION)
                    .setState(playbackState, getPosition(), 1f)
                    .build()
            )

            launch {
                when (playbackState) {
                    PlaybackStateCompat.STATE_PLAYING -> {
                        LMusicRuntime.updatePosition(getPosition(), true)
                        noisyReceiver.registerTo(this@LMusicService)
                        this@LMusicService.startForeground()
                        mNotificationManager.updateNotification(
                            mediaSession = mediaSession,
                            data = LMusicRuntime.currentPlaying
                        ) {
                            startForeground(NOTIFICATION_PLAYER_ID, it)
                        }
                    }
                    PlaybackStateCompat.STATE_PAUSED -> {
                        LMusicRuntime.updatePosition(getPosition(), false)
                        noisyReceiver.unRegisterFrom(this@LMusicService)
                        audioFocusHelper.abandonAudioFocus()
                        this@LMusicService.stopForeground()
                        mNotificationManager.updateNotification(
                            mediaSession = mediaSession,
                            data = LMusicRuntime.currentPlaying
                        )
                    }
                    else -> return@launch
                }
            }
        }

        override fun setRepeatMode(repeatMode: Int) {

        }

        override fun setShuffleMode(shuffleMode: Int) {

        }
    }

    fun startForeground() {
        val intent = Intent(this, LMusicService::class.java)
        ContextCompat.startForegroundService(this@LMusicService, intent)
    }

    fun stopForeground() {
        stopForeground(NotificationCompat.FOREGROUND_SERVICE_DEFERRED)
    }

    override fun onCreate() {
        super.onCreate()

        val sessionActivityPendingIntent =
            packageManager?.getLaunchIntentForPackage(packageName)?.let { sessionIntent ->
                PendingIntent.getActivity(this, 0, sessionIntent, 0)
            }

        mediaSession = MediaSessionCompat(this, "LMusicService")
            .apply {
                setSessionActivity(sessionActivityPendingIntent)
                setCallback(playBack)
                isActive = true
            }

        sessionToken = mediaSession.sessionToken
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        println("onStartCommand")

        MediaButtonReceiver.handleIntent(mediaSession, intent)
        return super.onStartCommand(intent, flags, startId)
    }

    /**
     * 鉴权判断是否允许访问媒体库
     */
    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot {
        return BrowserRoot("MAIN", null)
    }

    /**
     * 根据请求返回对应的媒体数据
     */
    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        result.sendResult(mutableListOf())
    }
}