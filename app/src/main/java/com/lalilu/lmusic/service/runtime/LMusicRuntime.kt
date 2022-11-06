package com.lalilu.lmusic.service.runtime

import com.lalilu.lmedia.entity.HISTORY_TYPE_SONG
import com.lalilu.lmedia.entity.LHistory
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmedia.indexer.Library
import com.lalilu.lmusic.repository.HistoryDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext

/**
 * 全局单例，专门用于解决Service和Activity之间复杂的数据交互问题
 */
object LMusicRuntime : LMusicBaseRuntime(), CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Default + SupervisorJob()
    private var historyStore: HistoryDataStore? = null

    fun init(historyDataStore: HistoryDataStore) {
        historyStore = historyDataStore
    }

    override suspend fun onSongsUpdate(songs: List<LSong>) {
        historyStore?.apply { lastPlayedListIdsKey.set(songs.map { it.id }) }
    }

    override suspend fun onPlayingUpdate(song: LSong?) {
        historyStore?.apply { lastPlayedIdKey.set(song?.id) }
        if (song?.id == null) return
        Library.historyRepo?.saveHistory(
            LHistory(
                contentId = song.id,
                type = HISTORY_TYPE_SONG
            )
        )
    }

    override suspend fun onPositionUpdate(position: Long) {
        historyStore?.apply { lastPlayedPositionKey.set(position) }
    }

    fun updatePosition(startValue: Long = -1, loop: Boolean = false) {
        updatePosition(startPosition = startValue, loopDelay = if (loop) 200 else 0)
    }
}