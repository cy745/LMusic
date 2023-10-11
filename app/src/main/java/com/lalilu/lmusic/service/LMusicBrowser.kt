package com.lalilu.lmusic.service

import android.content.ComponentName
import android.content.Context
import android.support.v4.media.MediaBrowserCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.lalilu.lmedia.LMedia
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmusic.datastore.LastPlayedSp
import com.lalilu.lplayer.LPlayer
import com.lalilu.lplayer.extensions.PlayerAction
import com.lalilu.lplayer.extensions.action
import com.lalilu.lplayer.extensions.getNextOf

class LMusicBrowser(
    private val context: Context,
    private val lastPlayedSp: LastPlayedSp,
) : DefaultLifecycleObserver {
    private val browser: MediaBrowserCompat by lazy {
        MediaBrowserCompat(
            context,
            ComponentName(context, LMusicService::class.java),
            MediaBrowserCompat.ConnectionCallback(),
            null
        )
    }

    fun setSongs(songs: List<LSong>, song: LSong? = null) {
        setSongs(mediaIds = songs.map { it.id }, mediaId = song?.id)
    }

    fun setSongs(mediaIds: List<String>, mediaId: String? = null) {
        LPlayer.runtime.queue.setIds(mediaIds)
        if (mediaIds.contains(mediaId)) {
            LPlayer.runtime.queue.setCurrentId(mediaId)
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        browser.connect()
        reloadItems()
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
        val nowIndex = LPlayer.runtime.queue.items.indexOf(mediaId)
        val currentIndex = LPlayer.runtime.queue.items.indexOf(LPlayer.runtime.queue.playingId)
        if (currentIndex >= 0 && (currentIndex == nowIndex || (currentIndex + 1) == nowIndex))
            return false

        if (nowIndex >= 0) {
            LPlayer.runtime.queue.moveByIndex(nowIndex, currentIndex)
        } else {
            LPlayer.runtime.queue.addToIndex(currentIndex + 1, mediaId)
        }
        return true
    }

    fun removeById(mediaId: String): Boolean {
        return try {
            val playingId = LPlayer.runtime.queue.playingId

            if (mediaId == playingId) {
                val nextId = LPlayer.runtime.queue.items.getNextOf(playingId, true)
                nextId?.let {
                    LPlayer.runtime.queue.setCurrentId(it)
                    playById(it)
                }
            }
            LPlayer.runtime.queue.removeById(mediaId)
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun reloadItems() {
        // 若当前播放列表不为空，则不尝试提取历史数据填充
        if (LPlayer.runtime.queue.items.isNotEmpty()) {
            return
        }

        val songIds by lastPlayedSp.lastPlayedListIdsKey
        val lastPlayedIdKey by lastPlayedSp.lastPlayedIdKey

        // 存在历史记录
        if (songIds.isNotEmpty()) {
            setSongs(songIds, lastPlayedIdKey)
            return
        }

        LMedia.whenReady {
            val songs = LMedia.get<LSong>()
            setSongs(songs, songs.getOrNull(0))
        }
    }
}