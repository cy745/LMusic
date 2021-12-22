package com.lalilu.lmusic.service

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ServiceLifecycleDispatcher
import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaButtonReceiver
import com.lalilu.lmusic.event.DataModule
import com.lalilu.lmusic.manager.LMusicNotificationManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class MSongService : MediaBrowserServiceCompat(), LifecycleOwner, CoroutineScope,
    Playback.OnPlayerCallback {

    companion object {
        const val MEDIA_ID_EMPTY_ROOT = "media_id_empty_root"
        const val ACTION_PLAY_PAUSE = "play_and_pause"

        const val defaultActions = PlaybackStateCompat.ACTION_PLAY or
                PlaybackStateCompat.ACTION_PLAY_PAUSE or
                PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID or
                PlaybackStateCompat.ACTION_PAUSE or
                PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS

        const val defaultFlags = MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
    }

    private val tag = this.javaClass.name
    override val coroutineContext: CoroutineContext get() = Dispatchers.Default
    override fun getLifecycle(): Lifecycle = ServiceLifecycleDispatcher(this).lifecycle
    private lateinit var mediaSession: MediaSessionCompat

    @Inject
    lateinit var dataModule: DataModule

    @Inject
    lateinit var mSessionCallback: LMusicSessionCompactCallback

    @Inject
    lateinit var mNotificationManager: LMusicNotificationManager

    override fun onCreate() {
        super.onCreate()

        val sessionActivityPendingIntent = packageManager
            ?.getLaunchIntentForPackage(packageName)
            ?.let { sessionIntent ->
                PendingIntent.getActivity(
                    this, 0,
                    sessionIntent, PendingIntent.FLAG_UPDATE_CURRENT
                )
            }

        mediaSession = MediaSessionCompat(this, tag).apply {
            setFlags(defaultFlags)
            setSessionActivity(sessionActivityPendingIntent)
            setCallback(mSessionCallback)
            setSessionToken(sessionToken)
        }
        mSessionCallback.mediaSession = mediaSession
        mSessionCallback.playback.onPlayerCallback = this
    }

    override fun onPlaybackStateChanged(newState: Int) {
        val position = mSessionCallback.playback.getPosition()
        val state = PlaybackStateCompat.Builder()
            .setActions(defaultActions)
            .setState(newState, position, 1.0f)
            .build()
        mediaSession.setPlaybackState(state)

        try {
            val notification = mNotificationManager.getNotification(mediaSession)
            when (state.state) {
                PlaybackStateCompat.STATE_PLAYING -> {
                    val intent = Intent(this, MSongService::class.java)
                    ContextCompat.startForegroundService(this, intent)
                    startForeground(LMusicNotificationManager.NOTIFICATION_ID, notification)
                }
                PlaybackStateCompat.STATE_PAUSED -> {
                    stopForeground(false)
                    mNotificationManager.notificationManager.notify(
                        LMusicNotificationManager.NOTIFICATION_ID, notification
                    )
                }
                PlaybackStateCompat.STATE_STOPPED -> {
                    stopForeground(true)
                    stopSelf()
                }
                else -> return
            }
        } catch (e: Exception) {
            println(e.message)
        }
    }

    override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
        mediaSession.setMetadata(metadata)
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
        result.detach()
        launch {
            mediaSession.setPlaybackState(mediaSession.controller.playbackState)
            mediaSession.setMetadata(mediaSession.controller.metadata)

            dataModule.nowPlaylistMediaItemFlow.collect {
                result.sendResult(it.toMutableList())
            }
        }
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