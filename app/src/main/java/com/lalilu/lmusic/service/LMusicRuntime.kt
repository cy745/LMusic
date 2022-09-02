package com.lalilu.lmusic.service

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.asLiveData
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmusic.repository.HistoryDataStore
import com.lalilu.lmusic.utils.extension.moveHeadToTailWithSearch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.*
import kotlin.concurrent.timerTask
import kotlin.coroutines.CoroutineContext

/**
 * 全局单例，专门用于解决Service和Activity之间复杂的数据交互问题
 */
object LMusicRuntime : CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Default
    private var historyStore: HistoryDataStore? = null

    fun init(historyDataStore: HistoryDataStore) {
        historyStore = historyDataStore
    }

    var currentPlaylist: List<LSong> = emptyList()
        set(value) {
            field = value
            launch {
                currentPlaylistFlow.emit(value)
                historyStore?.apply { lastPlayedListIdsKey.set(value.map { it.id }) }
            }
        }
    var currentPlaying: LSong? = null
        set(value) {
            field = value
            launch {
                currentPlayingFlow.emit(value)
                historyStore?.apply { lastPlayedIdKey.set(value?.id) }
            }
        }
    private var currentPosition: Long = 0
        set(value) {
            field = value
            launch {
                currentPositionFlow.emit(value)
                historyStore?.apply { lastPlayedPositionKey.set(value) }
            }
        }
    var currentIsPlaying: Boolean = false
        set(value) {
            field = value
            launch {
                currentIsPlayingFlow.emit(value)
            }
        }

    val currentPlaylistFlow: MutableStateFlow<List<LSong>> = MutableStateFlow(currentPlaylist)
    val currentPlayingFlow: MutableStateFlow<LSong?> = MutableStateFlow(currentPlaying)
    val currentPositionFlow: MutableStateFlow<Long> = MutableStateFlow(currentPosition)
    val currentIsPlayingFlow: MutableStateFlow<Boolean> = MutableStateFlow(currentIsPlaying)

    val currentPlayingLiveData = currentPlayingFlow.asLiveData()
    val currentPositionLiveData = currentPositionFlow.asLiveData()
    val currentPlaylistLiveData = currentPlaylistFlow.combine(currentPlayingFlow) { items, item ->
        item ?: return@combine items
        items.moveHeadToTailWithSearch(item.id) { listItem, id ->
            listItem.id == id
        }
    }.asLiveData()

    private var positionUpdateTimer: Timer? = null
    fun updatePosition(startValue: Long = -1, loop: Boolean = false) {
        if (startValue >= 0) {
            currentPosition = startValue
        }
        positionUpdateTimer?.cancel()
        positionUpdateTimer = null
        if (loop) {
            positionUpdateTimer = Timer()
            positionUpdateTimer?.schedule(
                timerTask { currentPosition += 1000 }, 0, 1000
            )
        }
    }

    var currentIsPLayingState: MutableState<Boolean> = mutableStateOf(false)
    var currentPlayingState: MutableState<LSong?> = mutableStateOf(null)
    var currentRepeatMode: Int = 0
    var currentShuffleMode: Int = 0
}