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

class LMusicServiceConnector(
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
        reloadItems()
        browser.connect()
    }

    override fun onStop(owner: LifecycleOwner) {
        browser.disconnect()
    }

    private fun reloadItems() {
        val queue = LPlayer.runtime.queue
        // 若当前播放列表不为空，则不尝试提取历史数据填充
        if (queue.getSize() != 0) {
            return
        }

        val songIds by lastPlayedSp.lastPlayedListIdsKey
        val lastPlayedIdKey by lastPlayedSp.lastPlayedIdKey

        // 存在历史记录
        if (songIds.isNotEmpty()) {
            queue.setIds(songIds)
            queue.setCurrentId(lastPlayedIdKey)
            return
        }

        LMedia.whenReady {
            val songs = LMedia.get<LSong>()
            queue.setIds(songs.map { it.id })
            queue.setCurrentId(songs.getOrNull(0)?.id)
        }
    }
}