package com.lalilu.lmusic.service.runtime

import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmusic.datastore.LastPlayedSp

/**
 * 全局单例，专门用于解决Service和Activity之间复杂的数据交互问题
 */
class LMusicRuntime(
    private val lastPlayedSp: LastPlayedSp
) : BaseRuntime(), Runtime.Listener {
    override var listener: Runtime.Listener? = this

    override fun onSongsUpdate(songs: List<LSong>) {
        lastPlayedSp.lastPlayedListIdsKey.set(songs.map { it.id })
    }

    override fun onPlayingUpdate(song: LSong?) {
        lastPlayedSp.lastPlayedIdKey.set(song?.id)
    }

    override fun onPositionUpdate(position: Long) {
        lastPlayedSp.lastPlayedPositionKey.set(position)
    }

    fun updatePosition(startValue: Long = -1, loop: Boolean = false) {
        updatePosition(startPosition = startValue, loopDelay = if (loop) 100 else 0)
    }
}