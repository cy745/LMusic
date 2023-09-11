package com.lalilu.lmusic.service

import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmusic.datastore.LastPlayedSp
import com.lalilu.lmusic.repository.LMediaRepository
import com.lalilu.lmusic.utils.extension.moveHeadToTailWithSearch
import com.lalilu.lplayer.runtime.Runtime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import java.util.Timer
import kotlin.coroutines.CoroutineContext

/**
 * 全局单例，专门用于解决Service和Activity之间复杂的数据交互问题
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LMusicRuntime(
    private val lastPlayedSp: LastPlayedSp,
    private val lMediaRepo: LMediaRepository,
) : Runtime<LSong>, Runtime.Listener,
    CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Default

    override val songsIdsFlow: MutableStateFlow<List<String>> = MutableStateFlow(emptyList())
    override val playingIdFlow: MutableStateFlow<String?> = MutableStateFlow(null)
    override val positionFlow: MutableStateFlow<Long> = MutableStateFlow(0L)
    override val isPlayingFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
    override var listener: Runtime.Listener? = this
    override var timer: Timer? = null

    val playingFlow: Flow<LSong?> = playingIdFlow
        .flatMapLatest { lMediaRepo.requireSongFlowById(it) }

    val songsFlow = songsIdsFlow.combine(playingIdFlow) { ids, id ->
        id ?: return@combine ids
        ids.moveHeadToTailWithSearch(id) { a, b -> a == b }
    }.flatMapLatest { ids ->
        lMediaRepo.getSongsFlow(Int.MAX_VALUE).mapLatest { songsMap ->
            ids.mapNotNull { id -> songsMap.firstOrNull { it.id == id } }
        }
    }

    override fun getItemById(mediaId: String?): LSong? {
        mediaId ?: return null
        return lMediaRepo.requireSong(mediaId)
    }

    override fun getPlaying(): LSong? {
        val mediaId = getPlayingId() ?: return null
        return lMediaRepo.requireSong(mediaId)
    }

    override fun getPreviousOf(item: LSong, cycle: Boolean): LSong? {
        val previousId = getPreviousOf(item.id, cycle) ?: return null
        return lMediaRepo.requireSong(previousId)
    }

    override fun getNextOf(item: LSong, cycle: Boolean): LSong? {
        val nextId = getNextOf(item.id, cycle) ?: return null
        return lMediaRepo.requireSong(nextId)
    }

    override fun getShuffle(): LSong? {
        // TODO 随机，去重复逻辑
        val index = songsIdsFlow.value.indices.randomOrNull() ?: return null
        return lMediaRepo.requireSong(songsIdsFlow.value[index])
    }

    override fun onSongsUpdate(songsIds: List<String>) {
        lastPlayedSp.lastPlayedListIdsKey.set(songsIds)
    }

    override fun onPlayingUpdate(songId: String?) {
        lastPlayedSp.lastPlayedIdKey.set(songId)
    }

    override fun onPositionUpdate(position: Long) {
        lastPlayedSp.lastPlayedPositionKey.set(position)
    }
}