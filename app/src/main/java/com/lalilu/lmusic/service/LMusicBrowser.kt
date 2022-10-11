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
import com.lalilu.lmedia.indexer.Library
import com.lalilu.lmusic.Config
import com.lalilu.lmusic.repository.HistoryDataStore
import com.lalilu.lmusic.service.runtime.LMusicRuntime

object LMusicBrowser : DefaultLifecycleObserver {
    private var controller: MediaControllerCompat? = null
    private var historyStore: HistoryDataStore? = null
    private lateinit var browser: MediaBrowserCompat

    fun init(application: Application, historyDataStore: HistoryDataStore) {
        browser = MediaBrowserCompat(
            application,
            ComponentName(application, LMusicService::class.java),
            ConnectionCallback(application),
            null
        )
        historyStore = historyDataStore
    }

    fun setSongs(songs: List<LSong>, song: LSong? = null) {
        LMusicRuntime.load(songs = songs, playing = song)
    }

    override fun onStart(owner: LifecycleOwner) {
        browser.connect()
    }

    override fun onStop(owner: LifecycleOwner) {
        browser.disconnect()
    }

    fun play() = controller?.transportControls?.play()
    fun pause() = controller?.transportControls?.pause()
    fun skipToNext() = controller?.transportControls?.skipToNext()
    fun skipToPrevious() = controller?.transportControls?.skipToPrevious()
    fun playById(id: String) = controller?.transportControls?.playFromMediaId(id, null)
    fun seekTo(position: Number) = controller?.transportControls?.seekTo(position.toLong())
    fun playPause() = controller?.transportControls
        ?.sendCustomAction(Config.ACTION_PLAY_AND_PAUSE, null)

    fun reloadAndPlay() = controller?.transportControls
        ?.sendCustomAction(Config.ACTION_RELOAD_AND_PLAY, null)

    fun addAndPlay(id: String) {
        addToNext(id)
        playById(id)
    }

    fun addToNext(mediaId: String): Boolean {
        val nowIndex = LMusicRuntime.indexOfSong(mediaId = mediaId)
        val currentIndex = LMusicRuntime.getPlayingIndex()
        if (currentIndex >= 0 && (currentIndex == nowIndex || (currentIndex + 1) == nowIndex))
            return false

        if (nowIndex >= 0) {
            LMusicRuntime.move(nowIndex, currentIndex)
        } else {
            val item = Library.getSongOrNull(mediaId) ?: return false
            LMusicRuntime.add(currentIndex + 1, item)
        }
        return true
    }

    fun removeById(mediaId: String): Boolean {
        return try {
            if (mediaId == LMusicRuntime.getPlayingId()) {
                skipToNext()
            }
            val index = LMusicRuntime.indexOfSong(mediaId)
            LMusicRuntime.removeAt(index)
            true
        } catch (e: Exception) {
            false
        }
    }

    private class ConnectionCallback(
        val context: Context
    ) : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            controller = MediaControllerCompat(context, browser.sessionToken)

            // 若当前播放列表为空，则尝试提取历史数据填充
            if (LMusicRuntime.isEmpty()) {
                historyStore?.apply {
                    val songs = lastPlayedListIdsKey.get()
                        .mapNotNull { Library.getSongOrNull(it) }
                    val song = lastPlayedIdKey.get()
                        ?.let { Library.getSongOrNull(it) }
                    setSongs(songs, song)
                }
            }
            // 取消subscribe，可以解决Service和Activity通过Parcelable传递数据导致闪退的问题
            // browser.subscribe("ROOT", MusicSubscriptionCallback())
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