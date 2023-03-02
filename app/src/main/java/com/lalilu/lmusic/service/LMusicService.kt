package com.lalilu.lmusic.service

import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaButtonReceiver
import com.lalilu.lmedia.entity.HISTORY_TYPE_SONG
import com.lalilu.lmedia.entity.LHistory
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmedia.repository.HistoryRepository
import com.lalilu.lmusic.Config
import com.lalilu.lmusic.Config.MEDIA_DEFAULT_ACTION
import com.lalilu.lmusic.datastore.LMusicSp
import com.lalilu.lmusic.service.notification.LMusicNotifier
import com.lalilu.lmusic.service.playback.PlayMode
import com.lalilu.lmusic.service.playback.PlayQueue
import com.lalilu.lmusic.service.playback.Playback
import com.lalilu.lmusic.service.playback.helper.FadeVolumeProxy
import com.lalilu.lmusic.service.playback.helper.LMusicAudioFocusHelper
import com.lalilu.lmusic.service.playback.helper.LMusicNoisyReceiver
import com.lalilu.lmusic.service.playback.impl.LocalPlayer
import com.lalilu.lmusic.service.playback.impl.MixPlayback
import com.lalilu.lmusic.service.runtime.LMusicRuntime
import com.lalilu.lmusic.utils.EQHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.android.ext.android.inject
import kotlin.coroutines.CoroutineContext

class LMusicService : MediaBrowserServiceCompat(), CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Default

    private val lMusicSp: LMusicSp by inject()
    private val runtime: LMusicRuntime by inject()
    private val historyRepo: HistoryRepository by inject()
    private val audioFocusHelper: LMusicAudioFocusHelper by inject()
    private val noisyReceiver: LMusicNoisyReceiver by inject()
    private val localPlayer: LocalPlayer by inject()
    private val notifier: LMusicNotifier by inject()

    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var playback: MixPlayback

    private val intent: Intent by lazy {
        Intent(this@LMusicService, LMusicService::class.java)
    }

    fun startService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    fun stopForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            this@LMusicService.stopForeground(STOP_FOREGROUND_DETACH)
        } else {
            this@LMusicService.stopForeground(false)
        }
    }

    inner class PlaybackListener : Playback.Listener<LSong> {
        override fun onPlayInfoUpdate(item: LSong?, playbackState: Int, position: Long) {
            val isPlaying = playback.player?.isPlaying ?: false

            runtime.update(playing = item)
            runtime.update(isPlaying = isPlaying)
            runtime.updatePosition(startValue = position, loop = isPlaying)

            mediaSession.setMetadata(item?.metadataCompat)
            mediaSession.setPlaybackState(
                PlaybackStateCompat.Builder()
                    .setActions(MEDIA_DEFAULT_ACTION)
                    .setState(playbackState, position, 1f)
                    .build()
            )

            when (playbackState) {
                PlaybackStateCompat.STATE_PLAYING -> {
                    mediaSession.isActive = true
                    startService()
                    notifier.startForeground { id, notification ->
                        startForeground(id, notification)
                    }
                }

                PlaybackStateCompat.STATE_PAUSED -> {
                    // mediaSession.isActive = false
                    stopForeground()
                }

                PlaybackStateCompat.STATE_STOPPED -> {
                    mediaSession.isActive = false
                    stopSelf()
                    notifier.stopForeground {
                        stopForeground()
                        notifier.cancel()
                    }
                    return
                }
            }
            notifier.update()
        }

        override fun onSetPlayMode(playMode: PlayMode) {
            mediaSession.setRepeatMode(playMode.repeatMode)
            mediaSession.setShuffleMode(playMode.shuffleMode)
            notifier.update()
        }

        override fun onItemPlay(item: LSong) {
            historyRepo.saveHistory(
                LHistory(contentId = item.id, type = HISTORY_TYPE_SONG)
            )
        }
    }

    inner class LMusicRuntimeQueue : PlayQueue<LSong> {
        override fun getCurrent(): LSong? {
            return runtime.getPlaying() ?: runtime._songsFlow.value.getOrNull(0)
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
            runtime.update(playing = item)
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

        notifier.bindMediaSession(mediaSession)
        sessionToken = mediaSession.sessionToken

        lMusicSp.volumeControl.flow(true)
            .onEach {
                it ?: return@onEach

                FadeVolumeProxy.setMaxVolume(it)
                playback.setMaxVolume(it)
            }
            .launchIn(this)

        lMusicSp.enableSystemEq.flow(true)
            .onEach {
                EQHelper.setSystemEqEnable(it ?: false)
            }
            .launchIn(this)

        lMusicSp.playMode.flow(true)
            .onEach {
                it ?: return@onEach

                playback.onSetPlayMode(PlayMode.of(it))
            }
            .launchIn(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        println("[onStartCommand]: ${intent?.action}")

        val extras = intent?.extras
        when (intent?.action) {
            Config.ACTION_SET_REPEAT_MODE -> {
                extras?.apply {
                    val playMode = getInt(PlayMode.KEY)
                        .takeIf { it in 0..2 }
                        ?: return@apply

                    lMusicSp.playMode.set(playMode)
                }
            }
        }
        MediaButtonReceiver.handleIntent(mediaSession, intent)
        return START_STICKY
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

    override fun onDestroy() {
        // 服务被结束后取消本协程作用域
        this.cancel()
        super.onDestroy()
    }
}