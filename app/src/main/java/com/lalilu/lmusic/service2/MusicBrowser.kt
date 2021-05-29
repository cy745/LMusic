package com.lalilu.lmusic.service2

import android.app.Activity
import android.content.ComponentName
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat

class MusicBrowser constructor(private val context: Activity) {
    private var mediaBrowser: MediaBrowserCompat
    private var controllerCallback: MusicControllerCallback
    private var connectionCallback: MusicConnectionCallback
    private var subscriptionCallback: MusicSubscriptionCallback
    private lateinit var mediaController: MediaControllerCompat

    fun connect() = mediaBrowser.connect()
    fun disconnect() {
        if (mediaBrowser.isConnected) {
            mediaController.unregisterCallback(controllerCallback)
            mediaBrowser.disconnect()
        }
    }

    fun bindSongList() {

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
            println("[MediaBrowserCompat] #onConnected")
            mediaBrowser.sessionToken.also { token ->
                mediaController = MediaControllerCompat(context, token)
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
            println("[state]: " + state?.state)
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            println("[MusicControllerCallback]#onMetadataChanged")
        }
    }
}