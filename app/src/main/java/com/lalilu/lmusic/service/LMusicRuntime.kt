package com.lalilu.lmusic.service

import com.lalilu.lmedia.LMedia
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmusic.datastore.LastPlayedSp
import com.lalilu.lmusic.utils.extension.moveHeadToTailWithSearch
import com.lalilu.lplayer.runtime.Runtime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import java.util.Timer
import kotlin.coroutines.CoroutineContext

/**
 * 全局单例，专门用于解决Service和Activity之间复杂的数据交互问题
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LMusicRuntime(
    private val lastPlayedSp: LastPlayedSp
) : Runtime<LSong>, Runtime.Listener, CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Default

    override val songsIdsFlow: MutableStateFlow<List<String>> = MutableStateFlow(emptyList())
    override val playingIdFlow: MutableStateFlow<String?> = MutableStateFlow(null)
    override val positionFlow: MutableStateFlow<Long> = MutableStateFlow(0L)
    override val isPlayingFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
    override var listener: Runtime.Listener? = this
    override var timer: Timer? = null
    override var getPosition: () -> Long = { 0L }

    private val shuffleIgnoreHistoryCount = 20
    private val shuffleRetryCount = 5

    val playingFlow: Flow<LSong?> = playingIdFlow.flatMapLatest { LMedia.getFlow(it) }
    val songsFlow = songsIdsFlow.combine(playingIdFlow) { ids, id ->
        id ?: return@combine ids
        ids.moveHeadToTailWithSearch(id) { a, b -> a == b }
    }.flatMapLatest { LMedia.flowMapBy<LSong>(it) }

    override fun getItemById(mediaId: String?): LSong? {
        mediaId ?: return null
        return LMedia.get(mediaId)
    }

    override fun getPlaying(): LSong? {
        val mediaId = getPlayingId() ?: return null
        return LMedia.get(mediaId)
    }

    override fun getPreviousOf(item: LSong, cycle: Boolean): LSong? {
        val previousId = getPreviousOf(item.id, cycle) ?: return null
        return LMedia.get(previousId)
    }

    override fun getNextOf(item: LSong, cycle: Boolean): LSong? {
        val nextId = getNextOf(item.id, cycle) ?: return null
        return LMedia.get(nextId)
    }

    override fun getShuffle(): LSong? {
        val songIds = songsIdsFlow.value
        val playingIndex = getPlayingIndex()
        val endIndex = playingIndex + shuffleIgnoreHistoryCount
        var targetIndex: Int? = null
        var retryCount = shuffleRetryCount

        if (songIds.size <= shuffleIgnoreHistoryCount * 2) {
            while (true) {
                targetIndex = songIds.indices.randomOrNull() ?: break
                if (targetIndex != playingIndex || retryCount-- <= 0) break
            }
        } else {
            var targetRange = songIds.indices - playingIndex.rangeTo(endIndex)

            if (endIndex >= songIds.size) {
                targetRange = targetRange - 0.rangeTo(endIndex - songIds.size)
            }

            targetIndex = targetRange.randomOrNull()
        }

        targetIndex ?: return null
        return LMedia.get(songIds[targetIndex])
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