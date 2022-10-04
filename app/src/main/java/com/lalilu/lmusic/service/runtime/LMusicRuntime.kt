package com.lalilu.lmusic.service.runtime

import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmusic.datasource.MDataBase
import com.lalilu.lmusic.datasource.entity.PlayHistory
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
    private var mDataBase: MDataBase? = null

    fun init(
        historyDataStore: HistoryDataStore,
        mDataBase: MDataBase
    ) {
        historyStore = historyDataStore
        LMusicRuntime.mDataBase = mDataBase
    }

    override suspend fun onSongsUpdate(songs: List<LSong>) {
        historyStore?.apply { lastPlayedListIdsKey.set(songs.map { it.id }) }
    }

    override suspend fun onPlayingUpdate(song: LSong?) {
        historyStore?.apply { lastPlayedIdKey.set(song?.id) }
        if (song?.id == null) return
        mDataBase?.playHistoryDao()?.save(
            PlayHistory(mediaId = song.id, startTime = System.currentTimeMillis())
        )
    }

    override suspend fun onPositionUpdate(position: Long) {
        historyStore?.apply { lastPlayedPositionKey.set(position) }
    }

    fun updatePosition(startValue: Long = -1, loop: Boolean = false) {
        updatePosition(startPosition = startValue, loopDelay = if (loop) 200 else 0)
    }
}