package com.lalilu.lmusic.service.runtime

import com.lalilu.lmedia.entity.HISTORY_TYPE_SONG
import com.lalilu.lmedia.entity.LHistory
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmedia.repository.HistoryRepository
import com.lalilu.lmusic.datastore.LMusicSp

/**
 * 全局单例，专门用于解决Service和Activity之间复杂的数据交互问题
 */
class LMusicRuntime(
    private val lMusicSp: LMusicSp
) : BaseRuntime(), Runtime.Listener {
    override var listener: Runtime.Listener? = this

    override fun onSongsUpdate(songs: List<LSong>) {
        lMusicSp.lastPlayedListIdsKey.set(songs.map { it.id })
    }

    override fun onPlayingUpdate(song: LSong?) {
        lMusicSp.lastPlayedIdKey.set(song?.id)
    }

    override fun onPositionUpdate(position: Long) {
        lMusicSp.lastPlayedPositionKey.set(position)
    }

    fun updatePosition(startValue: Long = -1, loop: Boolean = false) {
        updatePosition(startPosition = startValue, loopDelay = if (loop) 200 else 0)
    }
}