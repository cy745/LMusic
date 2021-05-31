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
    private val context: Activity
) {
    var playbackStateCompat = MutableLiveData<PlaybackStateCompat>(null)
    var mediaMetadataCompat = MutableLiveData<MediaMetadataCompat>(null)

    private var mediaBrowser: MediaBrowserCompat
    private var controllerCallback: MusicControllerCallback
    private var connectionCallback: MusicConnectionCallback
    private var subscriptionCallback: MusicSubscriptionCallback
    lateinit var mediaController: MediaControllerCompat
    private lateinit var adapterToUpdate: UpdatableAdapter<MediaBrowserCompat.MediaItem>

    fun setAdapterToUpdate(adapterToUpdate: UpdatableAdapter<MediaBrowserCompat.MediaItem>) {
        this.adapterToUpdate = adapterToUpdate
        this.adapterToUpdate.setOnItemClickListener {
            println("[setOnItemClickListener]: ${it.mediaId}")
            mediaController.transportControls.playFromMediaId(it.mediaId, null)
            mediaController.transportControls.play()
        }
    }

    fun connect() = mediaBrowser.connect()
    fun disconnect() {
        mediaController.unregisterCallback(controllerCallback)
        mediaBrowser.disconnect()
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
    }

    inner class MusicConnectionCallback : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            println("[MusicConnectionCallback]#onConnected")
            mediaBrowser.sessionToken.also { token ->
                mediaController = MediaControllerCompat(context, token)
                MediaControllerCompat.setMediaController(context, mediaController)
            }
            mediaController.registerCallback(controllerCallback)
            mediaBrowser.subscribe(MusicService.Access_ID, subscriptionCallback)
        }
    }

    inner class MusicSubscriptionCallback : MediaBrowserCompat.SubscriptionCallback() {
        override fun onChildrenLoaded(
            parentId: String,
            children: MutableList<MediaBrowserCompat.MediaItem>
        ) {
            adapterToUpdate.updateList(children)
            mediaMetadataCompat.postValue(mediaController.metadata)
            playbackStateCompat.postValue(mediaController.playbackState)
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