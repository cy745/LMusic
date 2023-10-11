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
import com.lalilu.lplayer.extensions.QueueAction

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
    fun addToNext(mediaId: String): Boolean = QueueAction.AddToNext(mediaId).action()
    fun removeById(mediaId: String): Boolean {
        return try {
            val playingId = LPlayer.runtime.queue.getCurrentId()

            if (mediaId == playingId) {
                LPlayer.runtime.queue.getNextId()
                    ?.run { PlayerAction.SkipToNext.action() }
            }
            QueueAction.Remove(mediaId).action()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun setSongs(mediaIds: List<String>, mediaId: String? = null) {
        val queue = LPlayer.runtime.queue
        queue.setIds(mediaIds)
        if (mediaIds.contains(mediaId)) {
            queue.setCurrentId(mediaId)
        }
    }

    private fun reloadItems() {
        // 若当前播放列表不为空，则不尝试提取历史数据填充
        if (LPlayer.runtime.queue.getIds().isNotEmpty()) {
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
            setSongs(mediaIds = songs.map { it.id }, mediaId = songs.getOrNull(0)?.id)
        }
    }
}