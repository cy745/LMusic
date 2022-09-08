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
import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaButtonReceiver
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmusic.Config.MEDIA_DEFAULT_ACTION
import com.lalilu.lmusic.Config.MEDIA_STOPPED_STATE
import com.lalilu.lmusic.datasource.MDataBase
import com.lalilu.lmusic.repository.HistoryDataStore
import com.lalilu.lmusic.repository.SettingsDataStore
import com.lalilu.lmusic.utils.extension.getNextOf
import com.lalilu.lmusic.utils.extension.getPreviousOf
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LMusicService : MediaBrowserServiceCompat() {

    @Inject
    lateinit var historyDataStore: HistoryDataStore

    @Inject
    lateinit var settingsDataStore: SettingsDataStore

    @Inject
    lateinit var database: MDataBase

    lateinit var mediaSession: MediaSessionCompat

    private val mNotificationManager: LMusicNotificationImpl by lazy {
        LMusicNotificationImpl(this, database, playBack)
    }
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
        override fun getById(id: String): LSong? =
            LMusicRuntime.currentPlaylist.find { it.id == id }

        override fun requestAudioFocus(): Boolean {
            return audioFocusHelper.requestAudioFocus() == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }

        override fun getNext(random: Boolean): LSong? {
            settingsDataStore.apply {
                val current = getCurrent()
                return when (repeatModeKey.get()) {
                    PlaybackStateCompat.REPEAT_MODE_INVALID,
                    PlaybackStateCompat.REPEAT_MODE_NONE ->
                        LMusicRuntime.currentPlaylist.getNextOf(current, false)
                    PlaybackStateCompat.REPEAT_MODE_ONE -> current
                    else -> LMusicRuntime.currentPlaylist.getNextOf(current, true)
                }
            }
        }

        override fun getPrevious(): LSong? {
            settingsDataStore.apply {
                val current = getCurrent()
                return when (repeatModeKey.get()) {
                    PlaybackStateCompat.REPEAT_MODE_INVALID,
                    PlaybackStateCompat.REPEAT_MODE_NONE ->
                        LMusicRuntime.currentPlaylist.getPreviousOf(current, false)
                    PlaybackStateCompat.REPEAT_MODE_ONE -> current
                    else -> LMusicRuntime.currentPlaylist.getPreviousOf(current, true)
                }
            }
        }

        override fun onPlayingItemUpdate(item: LSong?) {
            LMusicRuntime.currentPlaying = item
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            mediaSession.setMetadata(metadata)
        }

        override fun onPlaybackStateChanged(@PlaybackStateCompat.State playbackState: Int) {
            when (playbackState) {
                PlaybackStateCompat.STATE_STOPPED -> {
                    LMusicRuntime.updatePosition(0, false)
                    LMusicRuntime.currentIsPlaying = false

                    noisyReceiver.unRegisterFrom(this@LMusicService)
                    audioFocusHelper.abandonAudioFocus()

                    mediaSession.isActive = false
                    mediaSession.setPlaybackState(MEDIA_STOPPED_STATE)

                    stopSelf()
                    mNotificationManager.cancelNotification()
                }
                PlaybackStateCompat.STATE_BUFFERING,
                PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS,
                PlaybackStateCompat.STATE_SKIPPING_TO_NEXT -> {
                    // 更新进度，进度置0
                    mediaSession.setPlaybackState(
                        PlaybackStateCompat.Builder()
                            .setActions(MEDIA_DEFAULT_ACTION)
                            .setState(playbackState, 0, 1f)
                            .build()
                    )
                }
                PlaybackStateCompat.STATE_PAUSED -> {
                    // 更新进度
                    mediaSession.setPlaybackState(
                        PlaybackStateCompat.Builder()
                            .setActions(MEDIA_DEFAULT_ACTION)
                            .setState(playbackState, getPosition(), 1f)
                            .build()
                    )

                    mediaSession.isActive = false
                    mNotificationManager.updateNotification(data = LMusicRuntime.currentPlaying)
                    noisyReceiver.unRegisterFrom(this@LMusicService)
                    audioFocusHelper.abandonAudioFocus()
                    this@LMusicService.stopForeground(false)

                    LMusicRuntime.updatePosition(getPosition(), false)
                    LMusicRuntime.currentIsPlaying = false
                }
                PlaybackStateCompat.STATE_PLAYING -> {
                    // 更新进度
                    mediaSession.setPlaybackState(
                        PlaybackStateCompat.Builder()
                            .setActions(MEDIA_DEFAULT_ACTION)
                            .setState(playbackState, getPosition(), 1f)
                            .build()
                    )

                    mediaSession.isActive = true
                    startService(Intent(this@LMusicService, LMusicService::class.java))
                    mNotificationManager.updateNotification(data = LMusicRuntime.currentPlaying)

                    noisyReceiver.registerTo(this@LMusicService)

                    // todo 暂停时seekTo会导致进度错误开始增加
                    LMusicRuntime.updatePosition(getPosition(), true)
                    LMusicRuntime.currentIsPlaying = true
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
                PendingIntent.getActivity(
                    this, 0, sessionIntent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
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
        return START_NOT_STICKY
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