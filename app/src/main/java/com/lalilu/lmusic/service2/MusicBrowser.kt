package com.lalilu.lmusic.service2

import android.app.Activity
import android.content.ComponentName
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.MutableLiveData
import com.lalilu.lmusic.adapter2.UpdatableAdapter

class MusicBrowser constructor(
    private val context: Activity,
    private val adapterToUpdate: UpdatableAdapter<MediaBrowserCompat.MediaItem>
) {
    var playbackStateCompat = MutableLiveData<PlaybackStateCompat>(null)
    var mediaMetadataCompat = MutableLiveData<MediaMetadataCompat>(null)

    private var mediaBrowser: MediaBrowserCompat
    private var controllerCallback: MusicControllerCallback
    private var connectionCallback: MusicConnectionCallback
    private var subscriptionCallback: MusicSubscriptionCallback
    private lateinit var mediaController: MediaControllerCompat

    fun isConnected() = mediaBrowser.isConnected
    fun connect() = mediaBrowser.connect()
    fun disconnect() {
        if (mediaBrowser.isConnected) {
            mediaController.unregisterCallback(controllerCallback)
            mediaBrowser.disconnect()
        }
    }

    init {
        connectionCallback = MusicConnectionCallback()
        controllerCallback = MusicControllerCallback()
        subscriptionCallback = MusicSubscriptionCallback()
        mediaBrowser = MediaBrowserCompat(
            context,
            ComponentName(context, MusicService::class.java),
            connectionCallback, null
        )
        adapterToUpdate.setOnItemClickListener {
            println("[setOnItemClickListener]: ${it.mediaId}")
            mediaController.transportControls.playFromMediaId(it.mediaId, null)
            mediaController.transportControls.play()
        }
    }

    inner class MusicConnectionCallback : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            println("[MusicConnectionCallback]#onConnected")
            mediaBrowser.sessionToken.also { token ->
                mediaController = MediaControllerCompat(context, token)
                mediaController.registerCallback(controllerCallback)
                MediaControllerCompat.setMediaController(context, mediaController)
            }
            mediaBrowser.subscribe(MusicService.Access_ID, subscriptionCallback)
        }
    }

    inner class MusicSubscriptionCallback : MediaBrowserCompat.SubscriptionCallback() {
        override fun onChildrenLoaded(
            parentId: String,
            children: MutableList<MediaBrowserCompat.MediaItem>
        ) {
            adapterToUpdate.updateList(children)
            println("[MusicSubscriptionCallback]#onChildrenLoaded: $parentId")
            for (item: MediaBrowserCompat.MediaItem in children) {
                println(item.mediaId + " || " + item.toString())
            }
        }
    }

    inner class MusicControllerCallback : MediaControllerCompat.Callback() {
        override fun onSessionReady() {
            println("[MusicControllerCallback]#onSessionReady")
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            playbackStateCompat.postValue(state ?: return)
            println("[MusicControllerCallback]#onPlaybackStateChanged: " + state.state)
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            mediaMetadataCompat.postValue(metadata ?: return)
            println("[MusicControllerCallback]#onMetadataChanged")
        }
    }
}