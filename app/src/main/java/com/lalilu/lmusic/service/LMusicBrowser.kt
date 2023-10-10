package com.lalilu.lmusic.service

import android.content.ComponentName
import android.content.Context
import android.support.v4.media.MediaBrowserCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.blankj.utilcode.util.LogUtils
import com.lalilu.lmedia.LMedia
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmusic.datastore.LastPlayedSp
import com.lalilu.lplayer.extensions.PlayerAction
import com.lalilu.lplayer.extensions.action
import com.lalilu.lplayer.extensions.getNextOf
import com.lalilu.lplayer.runtime.NewRuntime

class LMusicBrowser(
    private val context: Context,
    private val lastPlayedSp: LastPlayedSp,
) : DefaultLifecycleObserver {
    private val browser: MediaBrowserCompat by lazy {
        MediaBrowserCompat(
            context,
            ComponentName(context, LMusicService::class.java),
            ConnectionCallback(context),
            null
        )
    }

    fun setSongs(songs: List<LSong>, song: LSong? = null) {
        setSongs(mediaIds = songs.map { it.id }, mediaId = song?.id)
    }

    fun setSongs(mediaIds: List<String>, mediaId: String? = null) {
        NewRuntime.queue.setIds(mediaIds)
        if (mediaIds.contains(mediaId)) {
            NewRuntime.queue.setCurrentId(mediaId)
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        browser.connect()
    }

    override fun onStop(owner: LifecycleOwner) {
        browser.disconnect()
    }

    fun play() = PlayerAction.Play.action()
    fun pause() = PlayerAction.Pause.action()
    fun playOrPause() = PlayerAction.PlayOrPause.action()
    fun skipToNext() = PlayerAction.SkipToNext.action()
    fun skipToPrevious() = PlayerAction.SkipToPrevious.action()
    fun playById(id: String) = PlayerAction.PlayById(id).action()
    fun seekTo(position: Number) = PlayerAction.SeekTo(position.toLong()).action()

    fun addToNext(mediaId: String): Boolean {
        val nowIndex = NewRuntime.queue.items.indexOf(mediaId)
        val currentIndex = NewRuntime.queue.items.indexOf(NewRuntime.queue.playingId)
        if (currentIndex >= 0 && (currentIndex == nowIndex || (currentIndex + 1) == nowIndex))
            return false

        if (nowIndex >= 0) {
            NewRuntime.queue.moveByIndex(nowIndex, currentIndex)
        } else {
            NewRuntime.queue.addToIndex(currentIndex + 1, mediaId)
        }
        return true
    }

    fun removeById(mediaId: String): Boolean {
        return try {
            val playingId = NewRuntime.queue.playingId

            if (mediaId == playingId) {
                val nextId = NewRuntime.queue.items.getNextOf(playingId, true)
                nextId?.let {
                    NewRuntime.queue.setCurrentId(it)
                    playById(it)
                }
            }
            NewRuntime.queue.removeById(mediaId)
            true
        } catch (e: Exception) {
            false
        }
    }

    private inner class ConnectionCallback(val context: Context) :
        MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            LogUtils.i("MediaBrowser connected")

            // 若当前播放列表不为空，则不尝试提取历史数据填充
            if (NewRuntime.queue.items.isNotEmpty()) {
                return
            }

            val songIds by lastPlayedSp.lastPlayedListIdsKey
            val lastPlayedIdKey by lastPlayedSp.lastPlayedIdKey

            // 存在历史记录
            if (songIds.isNotEmpty()) {
                LMedia.whenReady {
                    val songs = LMedia.mapBy<LSong>(songIds)
                    val song = LMedia.get<LSong>(lastPlayedIdKey)
                    setSongs(songs, song)
                }
                return
            }

            LMedia.whenReady {
                val songs = LMedia.get<LSong>()
                setSongs(songs, songs.getOrNull(0))
            }
        }
    }
}