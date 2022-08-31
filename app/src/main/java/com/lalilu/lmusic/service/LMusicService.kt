package com.lalilu.lmusic.service

import android.app.PendingIntent
import android.content.Intent
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
        override fun requestAudioFocus(): Boolean = true
        override fun getUriFromItem(item: LSong): Uri = item.uri
        override fun getCurrent(): LSong? = LMusicRuntime.currentPlaying
        override fun getMetaDataFromItem(item: LSong?): MediaMetadataCompat? = item?.metadataCompat

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
                try {
                    LMusicRuntime.updatePosition(0, false)
                    stopForeground(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
                    mediaSession.setPlaybackState(MEDIA_STOPPED_STATE)
                    mediaSession.isActive = false
                    stopSelf()
//                    unregisterReceiver(noisyReceiver)
                } catch (ignored: Exception) {
                }
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
//                        this@MSongService.registerReceiver(noisyReceiver, Config.FILTER_BECOMING_NOISY)
                        val intent = Intent(this@LMusicService, LMusicService::class.java)
                        ContextCompat.startForegroundService(this@LMusicService, intent)
                        mNotificationManager.updateNotification(
                            mediaSession = mediaSession,
                            data = LMusicRuntime.currentPlaying
                        ) {
                            startForeground(NOTIFICATION_PLAYER_ID, it)
                        }
                    }
                    PlaybackStateCompat.STATE_PAUSED -> {
                        LMusicRuntime.updatePosition(getPosition(), false)
                        stopForeground(NotificationCompat.FOREGROUND_SERVICE_DEFERRED)
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

    private val MEDIA_DEFAULT_ACTION = PlaybackStateCompat.ACTION_PLAY or
            PlaybackStateCompat.ACTION_PLAY_PAUSE or
            PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID or
            PlaybackStateCompat.ACTION_PAUSE or
            PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
            PlaybackStateCompat.ACTION_STOP or
            PlaybackStateCompat.ACTION_SEEK_TO or
            PlaybackStateCompat.ACTION_SET_REPEAT_MODE

    private val MEDIA_STOPPED_STATE = PlaybackStateCompat.Builder()
        .setState(PlaybackStateCompat.STATE_STOPPED, 0, 1f)
        .build()
}