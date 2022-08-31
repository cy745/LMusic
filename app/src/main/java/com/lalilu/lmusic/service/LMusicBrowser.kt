package com.lalilu.lmusic.service

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.blankj.utilcode.util.LogUtils
import com.lalilu.lmedia.entity.LSong

object LMusicBrowser : DefaultLifecycleObserver {
    private var controller: MediaControllerCompat? = null
    private lateinit var browser: MediaBrowserCompat

    fun init(application: Application) {
        browser = MediaBrowserCompat(
            application,
            ComponentName(application, LMusicService::class.java),
            ConnectionCallback(application),
            null
        )
    }

    fun setSongs(songs: List<LSong>, song: LSong? = null) {
        LMusicRuntime.currentPlaylist.clear()
        LMusicRuntime.currentPlaylist.addAll(songs)
        if (song != null && songs.contains(song)) {
            LMusicRuntime.currentPlaying = song
            LMusicRuntime.updatePlaying()
        }
        LMusicRuntime.updatePlaylist()
    }

    override fun onStart(owner: LifecycleOwner) {
        browser.connect()
    }

    override fun onStop(owner: LifecycleOwner) {
        browser.disconnect()
    }

    fun play() = controller?.transportControls?.play()
    fun pause() = controller?.transportControls?.pause()
    fun playPause() = controller?.transportControls?.sendCustomAction("PLAY_PAUSE", null)
    fun reloadAndPlay() = controller?.transportControls?.sendCustomAction("RELOAD_AND_PLAY", null)
    fun skipToNext() = controller?.transportControls?.skipToNext()
    fun skipToPrevious() = controller?.transportControls?.skipToPrevious()
    fun playById(id: String) = controller?.transportControls?.playFromMediaId(id, null)
    fun seekTo(position: Number) = controller?.transportControls?.seekTo(position.toLong())

    private class ConnectionCallback(
        val context: Context
    ) : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            controller = MediaControllerCompat(context, browser.sessionToken)
            // browser.subscribe("ROOT", MusicSubscriptionCallback())
            // todo 取消subscribe，可以解决Service和Activity通过Parcelable传递数据导致闪退的问题
        }
    }

    private class MusicSubscriptionCallback : MediaBrowserCompat.SubscriptionCallback() {
        override fun onChildrenLoaded(
            parentId: String,
            children: MutableList<MediaBrowserCompat.MediaItem>
        ) {
            LogUtils.d("[MusicSubscriptionCallback]#onChildrenLoaded: $parentId")
        }
    }
}