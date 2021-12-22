package com.lalilu.lmusic.service

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
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
import com.lalilu.lmusic.event.SharedViewModel
import com.lalilu.lmusic.manager.LMusicAudioFocusManager
import com.lalilu.lmusic.manager.LMusicNotificationManager
import com.lalilu.lmusic.manager.MusicNoisyReceiver
import com.lalilu.lmusic.state.LMusicServiceViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class MSongService : MediaBrowserServiceCompat(), LifecycleOwner, CoroutineScope {
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

        val becomingNoisyFilter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
    }

    override val coroutineContext: CoroutineContext get() = Dispatchers.Default
    override fun getLifecycle(): Lifecycle = ServiceLifecycleDispatcher(this).lifecycle

    private val tag = this.javaClass.name

    @Inject
    lateinit var mState: LMusicServiceViewModel

    @Inject
    lateinit var mEvent: SharedViewModel

    @Inject
    lateinit var mNotificationManager: LMusicNotificationManager

    @Inject
    lateinit var mAudioFocusManager: LMusicAudioFocusManager

    @Inject
    lateinit var mNoisyReceiver: MusicNoisyReceiver

    @Inject
    lateinit var dataModule: DataModule

    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mSessionCallback: LMusicSessionCompactCallback

    override fun onCreate() {
        super.onCreate()
        mSessionCallback = LMusicSessionCompactCallback()
        mNoisyReceiver.onBecomingNoisyListener = mSessionCallback
        mAudioFocusManager.onAudioFocusChangeListener = mSessionCallback

        val sessionActivityPendingIntent =
            packageManager?.getLaunchIntentForPackage(packageName)?.let { sessionIntent ->
                PendingIntent.getActivity(this, 0, sessionIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            }

        mediaSession = MediaSessionCompat(this, tag).apply {
            setFlags(defaultFlags)
            setSessionActivity(sessionActivityPendingIntent)
            setCallback(mSessionCallback)
            setSessionToken(sessionToken)
        }

        mEvent.nowPlaylistWithSongsRequest.getData().observeForever {
            mSessionCallback.playback.nowPlaylist.value = it
        }
        mState.playingSong.observeForever {
            mEvent.nowPlayingId.postValue(it.songId)
        }
    }


    inner class LMusicSessionCompactCallback : MediaSessionCompat.Callback(),
        Playback.OnPlayerCallback, AudioManager.OnAudioFocusChangeListener,
        MusicNoisyReceiver.OnBecomingNoisyListener {
        val playback = MSongPlayback(this@MSongService, mState)
            .setAudioFocusManager(mAudioFocusManager)
            .setOnPlayerCallback(this)

        override fun onPlaybackStateChanged(newState: Int) {
            val state = PlaybackStateCompat.Builder()
                .setActions(defaultActions)
                .setState(newState, playback.getPosition(), 1.0f)
                .build()
            mediaSession.setPlaybackState(state)

            val notification = mNotificationManager.getNotification(mediaSession)
            when (state.state) {
                PlaybackStateCompat.STATE_PLAYING -> {
                    val intent = Intent(this@MSongService, MSongService::class.java)
                    ContextCompat.startForegroundService(this@MSongService, intent)
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
        }

        override fun onCustomAction(action: String?, extras: Bundle?) {
            when (action) {
                ACTION_PLAY_PAUSE -> playback.playAndPause()
            }
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            if (!mediaSession.isActive) {
                registerReceiver(mNoisyReceiver, becomingNoisyFilter)
            }
            mediaSession.setMetadata(metadata)
            mediaSession.isActive = true
        }

        override fun onAudioFocusChange(focusChange: Int) {
            when (focusChange) {
                AudioManager.AUDIOFOCUS_LOSS,
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                    playback.pause()
                }
            }
        }

        override fun onBecomingNoisy() {
            playback.pause()
        }

        override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
            playback.playByMediaId(mediaId?.toLong())
        }

        override fun onPause() {
            playback.pause()
        }

        override fun onPlay() {
            playback.play()
        }

        override fun onSkipToNext() {
            playback.next()
        }

        override fun onSkipToPrevious() {
            playback.previous()
        }

        override fun onSeekTo(pos: Long) {
            playback.seekTo(pos)
        }

        override fun onStop() {
            mediaSession.isActive = false
            unregisterReceiver(mNoisyReceiver)
            playback.stop()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        MediaButtonReceiver.handleIntent(mediaSession, intent)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
//        if (parentId == MEDIA_ID_EMPTY_ROOT) {
//            result.detach()
//            result.sendResult(ArrayList())
//        }
        result.detach()
        launch {
            dataModule.nowPlaylist.collect {
                result.sendResult(it)
            }
        }
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot {
        println("onGetRoot: clientPackageName: $clientPackageName, clientUid: $clientUid")
        return BrowserRoot("normal", null)
    }
}