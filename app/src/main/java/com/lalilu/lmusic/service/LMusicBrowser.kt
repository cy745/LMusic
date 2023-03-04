package com.lalilu.lmusic.service

import android.content.ComponentName
import android.content.Context
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.blankj.utilcode.util.LogUtils
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmusic.Config
import com.lalilu.lmusic.datastore.LastPlayedSp
import com.lalilu.lmusic.repository.LMediaRepository
import com.lalilu.lmusic.service.runtime.LMusicRuntime

class LMusicBrowser(
    private val context: Context,
    private val lastPlayedSp: LastPlayedSp,
    private val lMediaRepo: LMediaRepository,
    private val runtime: LMusicRuntime
) : DefaultLifecycleObserver {
    private var controller: MediaControllerCompat? = null
    private val browser: MediaBrowserCompat by lazy {
        MediaBrowserCompat(
            context,
            ComponentName(context, LMusicService::class.java),
            ConnectionCallback(context),
            null
        )
    }

    fun setSongs(songs: List<LSong>, song: LSong? = null) {
        runtime.load(songs = songs, playing = song)
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
        val nowIndex = runtime.indexOfSong(mediaId = mediaId)
        val currentIndex = runtime.getPlayingIndex()
        if (currentIndex >= 0 && (currentIndex == nowIndex || (currentIndex + 1) == nowIndex))
            return false

        if (nowIndex >= 0) {
            runtime.move(nowIndex, currentIndex)
        } else {
            val item = lMediaRepo.requireSong(mediaId = mediaId) ?: return false
            runtime.add(currentIndex + 1, item)
        }
        return true
    }

    fun removeById(mediaId: String): Boolean {
        return try {
            if (mediaId == runtime.getPlayingId()) {
                runtime.getNextOf(runtime.getPlaying(), true)?.let {
                    runtime.update(it)
                    playById(it.id)
                }
            }
            runtime.remove(mediaId)
            true
        } catch (e: Exception) {
            false
        }
    }

    private inner class ConnectionCallback(
        val context: Context
    ) : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            controller = MediaControllerCompat(context, browser.sessionToken)

            // 若当前播放列表不为空，则不尝试提取历史数据填充
            if (!runtime.isEmpty()) {
                return
            }

            val songIds by lastPlayedSp.lastPlayedListIdsKey
            val lastPlayedIdKey by lastPlayedSp.lastPlayedIdKey

            if (songIds.isNotEmpty()) {
                val songs = songIds.mapNotNull { lMediaRepo.requireSong(mediaId = it) }
                val song = lMediaRepo.requireSong(mediaId = lastPlayedIdKey)
                setSongs(songs, song)
                return
            }


            val songs = lMediaRepo.getSongs()
            setSongs(songs, songs.getOrNull(0))

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