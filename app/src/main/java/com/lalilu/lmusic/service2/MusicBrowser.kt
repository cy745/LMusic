package com.lalilu.lmusic.service2

import android.app.Activity
import android.content.ComponentName
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.lalilu.lmusic.fragment.LMusicViewModel
import java.util.logging.Logger

class MusicBrowser constructor(private val context: Activity) {
    private val logger = Logger.getLogger(this.javaClass.name)

    private var mViewModel: LMusicViewModel
    private var mediaBrowser: MediaBrowserCompat
    private var controllerCallback: MusicControllerCallback
    private var connectionCallback: MusicConnectionCallback
    private var subscriptionCallback: MusicSubscriptionCallback
    lateinit var mediaController: MediaControllerCompat

    fun connect() = mediaBrowser.connect()
    fun disconnect() {
        mediaController.unregisterCallback(controllerCallback)
        mediaBrowser.disconnect()
    }

    init {
        connectionCallback = MusicConnectionCallback()
        controllerCallback = MusicControllerCallback()
        subscriptionCallback = MusicSubscriptionCallback()
        mViewModel = LMusicViewModel.getInstance(null)
        mediaBrowser = MediaBrowserCompat(
            context, ComponentName(context, MusicService::class.java),
            connectionCallback, null
        )
    }

    inner class MusicConnectionCallback : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            logger.info("[MusicConnectionCallback]#onConnected")
            mediaBrowser.sessionToken.also { token ->
                mediaController = MediaControllerCompat(context, token)
                MediaControllerCompat.setMediaController(context, mediaController)
            }
            mediaController.registerCallback(controllerCallback)
            mediaBrowser.subscribe(MusicService.ACCESS_ID, subscriptionCallback)
        }
    }

    inner class MusicSubscriptionCallback : MediaBrowserCompat.SubscriptionCallback() {
        override fun onChildrenLoaded(
            parentId: String,
            children: MutableList<MediaBrowserCompat.MediaItem>
        ) {
            mViewModel.mediaList.postValue(children)
            mViewModel.mediaController.postValue(this@MusicBrowser.mediaController)
            mViewModel.metadata.postValue(mediaController.metadata)
            mViewModel.playBackState.postValue(mediaController.playbackState)
            logger.info("[MusicSubscriptionCallback]#onChildrenLoaded: $parentId")
        }
    }

    inner class MusicControllerCallback : MediaControllerCompat.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            mViewModel.playBackState.postValue(state ?: return)
            logger.info("[MusicControllerCallback]#onPlaybackStateChanged: " + state.state)
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            mViewModel.metadata.postValue(metadata ?: return)
            logger.info("[MusicControllerCallback]#onMetadataChanged")
        }
    }
}