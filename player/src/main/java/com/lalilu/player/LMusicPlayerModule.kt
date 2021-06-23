package com.lalilu.player

import android.app.Activity
import android.app.Application
import android.content.ComponentName
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.lalilu.media.LMusicMediaModule
import com.lalilu.player.service.MusicService
import org.jetbrains.annotations.Nullable
import java.util.logging.Logger

class LMusicPlayerModule private constructor(application: Application) :
    AndroidViewModel(application) {
    private val logger = Logger.getLogger(this.javaClass.name)

    private val mediaDatabase = LMusicMediaModule.getInstance(application).database
    val playlist = mediaDatabase.playlistDao().getAllLiveData()

    val mediaList = MutableLiveData<MutableList<MediaBrowserCompat.MediaItem>>(null)
    val metadata = MutableLiveData<MediaMetadataCompat>(null)
    val playBackState = MutableLiveData<PlaybackStateCompat>(null)
    val mediaController = MutableLiveData<MediaControllerCompat>(null)

    private lateinit var mediaBrowser: MediaBrowserCompat
    private lateinit var controllerCallback: MusicControllerCallback
    private lateinit var connectionCallback: MusicConnectionCallback
    private lateinit var subscriptionCallback: MusicSubscriptionCallback

    fun initMusicBrowser(activity: Activity) {
        connectionCallback = MusicConnectionCallback(activity)
        controllerCallback = MusicControllerCallback()
        subscriptionCallback = MusicSubscriptionCallback()
        mediaBrowser = MediaBrowserCompat(
            activity, ComponentName(activity, MusicService::class.java),
            connectionCallback, null
        )
    }

    fun connect() = mediaBrowser.connect()
    fun disconnect() = mediaBrowser.disconnect().also {
        mediaController.value?.unregisterCallback(controllerCallback)
    }

    inner class MusicConnectionCallback(private val activity: Activity) :
        MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            var controller: MediaControllerCompat
            mediaBrowser.sessionToken.also { token ->
                controller = MediaControllerCompat(activity, token)
                MediaControllerCompat.setMediaController(activity, controller)
            }
            controller.registerCallback(controllerCallback)
            mediaBrowser.subscribe(MusicService.ACCESS_ID, subscriptionCallback)
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
            this@LMusicPlayerModule.mediaList.postValue(children)
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

    companion object {
        @Volatile
        private var instance: LMusicPlayerModule? = null

        @Throws(NullPointerException::class)
        fun getInstance(@Nullable application: Application?): LMusicPlayerModule {
            instance ?: synchronized(LMusicPlayerModule::class.java) {
                if (application == null) throw NullPointerException("No Application Context Input")
                instance = LMusicPlayerModule(application)
            }
            return instance!!
        }
    }
}