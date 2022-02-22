package com.lalilu.lmusic.service

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.content.ContextCompat
import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaButtonReceiver
import com.lalilu.lmusic.Config
import com.lalilu.lmusic.Config.MEDIA_STOPPED_STATE
import com.lalilu.lmusic.datasource.MediaSource
import com.lalilu.lmusic.event.DataModule
import com.lalilu.lmusic.manager.LMusicNotificationManager
import com.lalilu.lmusic.manager.LMusicNotificationManager.Companion.NOTIFICATION_PLAYER_ID
import com.lalilu.lmusic.manager.MusicNoisyReceiver
import com.lalilu.lmusic.service.playback.Playback
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class MSongService : MediaBrowserServiceCompat(), CoroutineScope,
    Playback.OnPlayerCallback, MusicNoisyReceiver.OnBecomingNoisyListener {
    override val coroutineContext: CoroutineContext = Dispatchers.IO

    @Inject
    @SessionActivityPendingIntent
    lateinit var pendingIntent: PendingIntent

    @Inject
    lateinit var mediaSession: MediaSessionCompat

    @Inject
    lateinit var noisyReceiver: MusicNoisyReceiver

    @Inject
    lateinit var mNotificationManager: LMusicNotificationManager

    @Inject
    lateinit var mSessionCallback: MSongSessionCallback

    @Inject
    lateinit var dataModule: DataModule

    @Inject
    lateinit var mediaSource: MediaSource

    override fun onCreate() {
        super.onCreate()

        mediaSession.also {
            it.setSessionActivity(pendingIntent)
            it.setCallback(mSessionCallback)
            this.sessionToken = it.sessionToken
        }
        mSessionCallback.playback.onPlayerCallback = this
        noisyReceiver.onBecomingNoisyListener = this

        dataModule.repeatMode.observeForever {
            mediaSession.setRepeatMode(it)
        }
    }

    override fun onBecomingNoisy() {
        mediaSession.controller.transportControls.pause()
    }

    override fun onPlaybackStateChanged(newState: Int) {
        if (newState == PlaybackStateCompat.STATE_STOPPED) {
            try {
                stopForeground(true)
                stopSelf()
                mediaSession.setPlaybackState(MEDIA_STOPPED_STATE)
                dataModule.updatePlaybackState(MEDIA_STOPPED_STATE)
                this.unregisterReceiver(noisyReceiver)
            } catch (ignored: Exception) {
            }
            return
        }

        val position = mSessionCallback.playback.getPosition()
        val state = PlaybackStateCompat.Builder()
            .setActions(Config.MEDIA_DEFAULT_ACTION)
            .setState(newState, position, 1.0f)
            .build()
        mediaSession.setPlaybackState(state)
        dataModule.updatePlaybackState(state)

        launch {
            val notification = mNotificationManager.getNotification(mediaSession)
            when (state.state) {
                PlaybackStateCompat.STATE_PLAYING -> {
                    this@MSongService.registerReceiver(noisyReceiver, Config.FILTER_BECOMING_NOISY)
                    val intent = Intent(this@MSongService, MSongService::class.java)
                    ContextCompat.startForegroundService(this@MSongService, intent)
                    startForeground(NOTIFICATION_PLAYER_ID, notification)
                }
                PlaybackStateCompat.STATE_PAUSED -> {
                    stopForeground(false)
                    mNotificationManager.pushNotification(NOTIFICATION_PLAYER_ID, notification)
                }
                else -> return@launch
            }
        }
    }

    override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
        mediaSession.setMetadata(metadata)
        dataModule.updateMetadata(metadata)
        mediaSession.isActive = true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val customAction =
            intent?.extras?.getInt(PlaybackStateCompat.ACTION_SET_REPEAT_MODE.toString())
        when (customAction) {
            PlaybackStateCompat.REPEAT_MODE_ONE,
            PlaybackStateCompat.REPEAT_MODE_ALL -> {
                dataModule.updateRepeatMode(customAction)
                mediaSession.setRepeatMode(customAction)
                onPlaybackStateChanged(mediaSession.controller.playbackState.state)
            }
        }

        MediaButtonReceiver.handleIntent(mediaSession, intent)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        result.sendResult(ArrayList())
        dataModule.updateMetadata(mediaSession.controller.metadata)
        dataModule.updatePlaybackState(mediaSession.controller.playbackState)
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot {
        println("MSongService: onGetRoot: clientPackageName: $clientPackageName, clientUid: $clientUid")
        return BrowserRoot("normal", null)
    }
}