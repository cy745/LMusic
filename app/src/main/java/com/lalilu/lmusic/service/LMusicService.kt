package com.lalilu.lmusic.service

import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaButtonReceiver
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmusic.Config
import com.lalilu.lmusic.Config.MEDIA_DEFAULT_ACTION
import com.lalilu.lmusic.datastore.SettingsDataStore
import com.lalilu.lmusic.repository.CoverRepository
import com.lalilu.lmusic.repository.LyricRepository
import com.lalilu.lmusic.service.notification.LMusicNotifier
import com.lalilu.lmusic.service.playback.PlayQueue
import com.lalilu.lmusic.service.playback.Playback
import com.lalilu.lmusic.service.playback.helper.FadeVolumeProxy
import com.lalilu.lmusic.service.playback.helper.LMusicAudioFocusHelper
import com.lalilu.lmusic.service.playback.helper.LMusicNoisyReceiver
import com.lalilu.lmusic.service.playback.impl.LocalPlayer
import com.lalilu.lmusic.service.playback.impl.MixPlayback
import com.lalilu.lmusic.service.runtime.LMusicRuntime
import com.lalilu.lmusic.utils.EQHelper
import com.lalilu.lmusic.utils.PlayMode
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

@AndroidEntryPoint
class LMusicService : MediaBrowserServiceCompat(), CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Default + SupervisorJob()

    @Inject
    lateinit var runtime: LMusicRuntime

    @Inject
    lateinit var settingsDataStore: SettingsDataStore

    @Inject
    lateinit var lyricRepo: LyricRepository

    @Inject
    lateinit var coverRepo: CoverRepository

    @Inject
    lateinit var noisyReceiver: LMusicNoisyReceiver

    @Inject
    lateinit var audioFocusHelper: LMusicAudioFocusHelper

    @Inject
    lateinit var localPlayer: LocalPlayer

    lateinit var mediaSession: MediaSessionCompat

    lateinit var playback: MixPlayback

    lateinit var notifier: LMusicNotifier

    inner class PlaybackListener : Playback.Listener<LSong> {
        override fun onPlayingItemUpdate(item: LSong?) {
            runtime.updatePlaying(item)
            mediaSession.setMetadata(item?.metadataCompat)
            notifier.update()
        }

        override fun onPlaybackStateChanged(playbackState: Int, position: Long) {
            runtime.isPlaying = playback.player?.isPlaying ?: false
            runtime.updatePosition(position, runtime.isPlaying)
            mediaSession.setPlaybackState(
                PlaybackStateCompat.Builder()
                    .setActions(MEDIA_DEFAULT_ACTION)
                    .setState(playbackState, position, 1f)
                    .build()
            )

            if (playbackState == PlaybackStateCompat.STATE_PLAYING) {
                mediaSession.isActive = true
                startService(Intent(this@LMusicService, LMusicService::class.java))
            }
            if (playbackState == PlaybackStateCompat.STATE_PAUSED) {
                mediaSession.isActive = false
                this@LMusicService.stopForeground(false)
            }
            if (playbackState == PlaybackStateCompat.STATE_STOPPED) {
                mediaSession.isActive = false
                notifier.cancel()
                stopSelf()
            }
            notifier.update()
        }

        override fun onSetRepeatMode(repeatMode: Int) {
            mediaSession.setRepeatMode(repeatMode)
            notifier.update()
        }

        override fun onSetShuffleMode(shuffleMode: Int) {
            mediaSession.setShuffleMode(shuffleMode)
            notifier.update()
        }
    }

    inner class LMusicRuntimeQueue : PlayQueue<LSong> {
        override fun getCurrent(): LSong? {
            return runtime.getPlaying()
        }

        override fun getPrevious(random: Boolean): LSong? {
            return runtime.getPreviousOf(getCurrent(), true)
        }

        override fun getNext(random: Boolean): LSong? {
            return runtime.getNextOf(getCurrent(), true)
        }

        override fun getById(id: String): LSong? {
            return runtime.getSongById(id)
        }

        override fun getUriFromItem(item: LSong): Uri {
            return item.uri
        }

        override fun setCurrent(item: LSong) {
            runtime.updatePlaying(item)
        }
    }

    override fun onCreate() {
        super.onCreate()

        playback = MixPlayback(
            noisyReceiver = noisyReceiver,
            audioFocusHelper = audioFocusHelper,
            playbackListener = PlaybackListener(),
            queue = LMusicRuntimeQueue(),
            player = localPlayer
        )

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
                setCallback(playback)
                isActive = true
            }

        notifier = LMusicNotifier(
            lyricRepo = lyricRepo,
            playback = playback,
            coverRepo = coverRepo,
            mediaSession = mediaSession,
            settingsDataStore = settingsDataStore,
            service = this
        )

        sessionToken = mediaSession.sessionToken

        settingsDataStore.apply {
            volumeControl.flow()
                .onEach {
                    it?.let {
                        FadeVolumeProxy.setMaxVolume(it)
                        playback.setMaxVolume(it)
                    }
                }
                .launchIn(this@LMusicService)

            enableSystemEq.flow()
                .onEach { EQHelper.setSystemEqEnable(it ?: false) }
                .launchIn(this@LMusicService)

            playMode.flow()
                .onEach {
                    it ?: return@onEach

                    PlayMode.of(it).apply {
                        playback.onSetRepeatMode(repeatMode)
                        playback.onSetShuffleMode(shuffleMode)
                    }
                }.launchIn(this@LMusicService)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        println("onStartCommand: ${intent?.action} ${intent?.extras?.getInt(PlayMode.KEY)}")

        intent?.takeIf { it.action === Config.ACTION_SET_REPEAT_MODE }?.extras?.apply {
            val playMode = getInt(PlayMode.KEY)
                .takeIf { it in 0..2 }
                ?: return@apply

            settingsDataStore.apply { this.playMode.set(playMode) }
        }

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