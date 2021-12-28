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
import com.lalilu.lmusic.event.DataModule
import com.lalilu.lmusic.manager.LMusicNotificationManager
import com.lalilu.lmusic.manager.MusicNoisyReceiver
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class MSongService : MediaBrowserServiceCompat(), CoroutineScope,
    Playback.OnPlayerCallback, MusicNoisyReceiver.OnBecomingNoisyListener {

    override val coroutineContext: CoroutineContext = Dispatchers.IO

    @Inject
    lateinit var dataModule: DataModule

    @Inject
    lateinit var mSessionCallback: LMusicSessionCompactCallback

    @Inject
    lateinit var mNotificationManager: LMusicNotificationManager

    @Inject
    @SessionActivityPendingIntent
    lateinit var pendingIntent: PendingIntent

    @Inject
    lateinit var mediaSession: MediaSessionCompat

    @Inject
    lateinit var noisyReceiver: MusicNoisyReceiver

    override fun onCreate() {
        super.onCreate()

        mediaSession.also {
            it.setSessionActivity(pendingIntent)
            it.setCallback(mSessionCallback)
            this.sessionToken = it.sessionToken
        }
        mSessionCallback.playback.onPlayerCallback = this
        noisyReceiver.onBecomingNoisyListener = this
    }

    override fun onBecomingNoisy() {
        mediaSession.controller.transportControls.pause()
    }

    override fun onPlaybackStateChanged(newState: Int) {
        val position = mSessionCallback.playback.getPosition()
        val state = PlaybackStateCompat.Builder()
            .setActions(Config.MEDIA_DEFAULT_ACTION)
            .setState(newState, position, 1.0f)
            .build()
        mediaSession.setPlaybackState(state)
        dataModule.updatePlaybackState(state)

        val notification = mNotificationManager.getNotification(mediaSession)
        when (state.state) {
            PlaybackStateCompat.STATE_PLAYING -> {
                this.registerReceiver(noisyReceiver, Config.FILTER_BECOMING_NOISY)
                val intent = Intent(this, MSongService::class.java)
                ContextCompat.startForegroundService(this, intent)
                startForeground(LMusicNotificationManager.NOTIFICATION_PLAYER_ID, notification)
            }
            PlaybackStateCompat.STATE_PAUSED -> {
                stopForeground(false)
                mNotificationManager.notificationManager.notify(
                    LMusicNotificationManager.NOTIFICATION_PLAYER_ID, notification
                )
            }
            PlaybackStateCompat.STATE_STOPPED -> {
                this.unregisterReceiver(noisyReceiver)
                stopForeground(true)
                stopSelf()
            }
            else -> return
        }
    }

    override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
        mediaSession.setMetadata(metadata)
        dataModule.updateMetadata(metadata)
        mediaSession.isActive = true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        MediaButtonReceiver.handleIntent(mediaSession, intent)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        var loaded = false
        if (dataModule.nowPlaylistMediaItemLiveData.hasObservers())
            dataModule.nowPlaylistMediaItemLiveData.removeObserver {}

        dataModule.nowPlaylistMediaItemLiveData.observeForever {
            if (loaded) return@observeForever
            mediaSession.setPlaybackState(mediaSession.controller.playbackState)
            mediaSession.setMetadata(mediaSession.controller.metadata)
            result.sendResult(it?.toMutableList())
            loaded = true
        }

        if (!loaded) result.detach()
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