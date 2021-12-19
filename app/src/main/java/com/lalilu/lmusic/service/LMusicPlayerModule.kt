package com.lalilu.lmusic.service

import android.content.ComponentName
import android.content.Context
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.logging.Logger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LMusicPlayerModule @Inject constructor(
    @ApplicationContext context: Context
) : ViewModel() {
    private val logger = Logger.getLogger(this.javaClass.name)

    val metadata = MutableLiveData<MediaMetadataCompat>(null)
    val playBackState = MutableLiveData<PlaybackStateCompat>(null)
    val mediaController = MutableLiveData<MediaControllerCompat>(null)

    private var controllerCallback: MusicControllerCallback = MusicControllerCallback()
    private var connectionCallback: MusicConnectionCallback = MusicConnectionCallback(context)
    private var subscriptionCallback: MusicSubscriptionCallback = MusicSubscriptionCallback()
    private var mediaBrowser: MediaBrowserCompat = MediaBrowserCompat(
        context, ComponentName(context, MSongService::class.java),
        connectionCallback, null
    )

    fun connect() = mediaBrowser.connect()
    fun disconnect() = mediaBrowser.disconnect().also {
        mediaController.value?.unregisterCallback(controllerCallback)
    }

    inner class MusicConnectionCallback(private val context: Context) :
        MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            val controller = MediaControllerCompat(context, mediaBrowser.sessionToken).apply {
                registerCallback(controllerCallback)
            }

            mediaBrowser.subscribe("ACCESS_ID", subscriptionCallback)
            mediaController.postValue(controller)
            logger.info("[MusicConnectionCallback]#onConnected")
        }
    }

    inner class MusicSubscriptionCallback : MediaBrowserCompat.SubscriptionCallback() {
        override fun onChildrenLoaded(
            parentId: String,
            children: MutableList<MediaBrowserCompat.MediaItem>
        ) {
            val controller = mediaController.value
            this@LMusicPlayerModule.mediaController.postValue(controller)
            this@LMusicPlayerModule.metadata.postValue(controller?.metadata)
            this@LMusicPlayerModule.playBackState.postValue(controller?.playbackState)
            logger.info("[MusicSubscriptionCallback]#onChildrenLoaded: $parentId")
        }
    }

    inner class MusicControllerCallback : MediaControllerCompat.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            this@LMusicPlayerModule.playBackState.postValue(state ?: return)
            logger.info("[MusicControllerCallback]#onPlaybackStateChanged: " + state.state)
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            this@LMusicPlayerModule.metadata.postValue(metadata ?: return)
            logger.info("[MusicControllerCallback]#onMetadataChanged")
        }
    }
}