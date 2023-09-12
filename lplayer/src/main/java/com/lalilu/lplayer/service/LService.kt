package com.lalilu.lplayer.service

import android.app.Notification
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaButtonReceiver
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lplayer.LPlayer
import com.lalilu.lplayer.extensions.AudioFocusHelper
import com.lalilu.lplayer.notification.Notifier
import com.lalilu.lplayer.playback.PlayMode
import com.lalilu.lplayer.playback.Playback
import com.lalilu.lplayer.playback.impl.LocalPlayer
import com.lalilu.lplayer.playback.impl.MixPlayback
import com.lalilu.lplayer.runtime.Runtime
import com.lalilu.lplayer.runtime.RuntimeQueue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.koin.android.ext.android.inject
import kotlin.coroutines.CoroutineContext

@Suppress("DEPRECATION")
abstract class LService : MediaBrowserServiceCompat(),
    LifecycleOwner, Playback.Listener<LSong>, CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Default + SupervisorJob()
    override val lifecycle: Lifecycle get() = registry
    private val registry by lazy { LifecycleRegistry(this) }

    abstract fun getStartIntent(): Intent
    abstract fun getLoopDelay(isPlaying: Boolean): Long

    private val sessionActivityPendingIntent by lazy {
        packageManager?.getLaunchIntentForPackage(packageName)?.let { sessionIntent ->
            PendingIntent.getActivity(
                this, 0, sessionIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
    }

    lateinit var mediaSession: MediaSessionCompat
    lateinit var playback: MixPlayback

    private val runtime: Runtime<LSong> by inject()
    private val notifier: Notifier by inject()
    private val localPlayer: LocalPlayer by inject()
    private val audioFocusHelper: AudioFocusHelper by inject()
    private val noisyReceiverIntentFilter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
    private val noisyReceiver: BroadcastReceiver by lazy {
        object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                playback.onPause()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        registry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        if (!this::playback.isInitialized) {
            runtime.getPosition = localPlayer::getPosition
            playback = MixPlayback(
                audioFocusHelper = audioFocusHelper,
                playbackListener = this,
                queue = RuntimeQueue(runtime),
                player = localPlayer
            )
        }

        if (!this::mediaSession.isInitialized) {
            mediaSession = MediaSessionCompat(this, "LService")
                .apply {
                    setSessionActivity(sessionActivityPendingIntent)
                    setCallback(playback)
                    isActive = true
                }
        }

        sessionToken = mediaSession.sessionToken
        registry.handleLifecycleEvent(Lifecycle.Event.ON_START)
    }

    override fun onPlayInfoUpdate(item: LSong?, playbackState: Int, position: Long) {
        val isPlaying = playback.player?.isPlaying ?: false

        runtime.update(playing = item?.id)
        runtime.update(isPlaying = isPlaying)
        runtime.updatePosition(startPosition = position, loopDelay = getLoopDelay(isPlaying))

        mediaSession.setMetadata(item?.metadataCompat)
        mediaSession.setPlaybackState(
            PlaybackStateCompat.Builder()
                .setActions(LPlayer.MEDIA_DEFAULT_ACTION)
                .setState(playbackState, position, 1f)
                .build()
        )

        when (playbackState) {
            PlaybackStateCompat.STATE_PLAYING -> {
                registerReceiver(noisyReceiver, noisyReceiverIntentFilter)
                mediaSession.isActive = true
                startService()
                notifier.startForeground(mediaSession) { id, notification ->
                    startForeGround(id, notification)
                }
            }

            PlaybackStateCompat.STATE_PAUSED -> {
                kotlin.runCatching {
                    unregisterReceiver(noisyReceiver)
                }
                // mediaSession.isActive = false
                // stopForeground()
            }

            PlaybackStateCompat.STATE_STOPPED -> {
                kotlin.runCatching {
                    unregisterReceiver(noisyReceiver)
                }
                mediaSession.isActive = false
                stopSelf()
                notifier.stopForeground {
                    notifier.cancel()
                    stopForeground()
                }
                return
            }
        }
        notifier.update(mediaSession)
    }

    override fun onSetPlayMode(playMode: PlayMode) {
        mediaSession.setRepeatMode(playMode.repeatMode)
        mediaSession.setShuffleMode(playMode.shuffleMode)
        notifier.update(mediaSession)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        MediaButtonReceiver.handleIntent(mediaSession, intent)
        return START_STICKY
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?,
    ): BrowserRoot {
        return BrowserRoot("MAIN", null)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>,
    ) {
        result.sendResult(mutableListOf())
    }

    override fun onDestroy() {
        registry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        playback.destroy()
        localPlayer.destroy()

        // 服务被结束后取消本协程作用域
        if (coroutineContext[Job] != null) {
            this.cancel()
        }
        super.onDestroy()
    }

    private fun startService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(getStartIntent())
        } else {
            startService(getStartIntent())
        }
    }

    private fun startForeGround(id: Int, notification: Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                id, notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        } else {
            startForeground(id, notification)
        }
    }

    private fun stopForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            this.stopForeground(STOP_FOREGROUND_DETACH)
        } else {
            this.stopForeground(false)
        }
    }
}