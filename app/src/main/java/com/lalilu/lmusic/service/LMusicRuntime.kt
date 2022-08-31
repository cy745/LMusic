package com.lalilu.lmusic.service

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.asLiveData
import com.lalilu.lmedia.entity.LSong
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

    var currentPlaylist: List<LSong> = emptyList()
        set(value) {
            field = value
            launch { currentPlaylistFlow.emit(currentPlaylist) }
        }
    var currentPlaying: LSong? = null
        set(value) {
            field = value
            launch { currentPlayingFlow.emit(currentPlaying) }
        }
    private var currentPosition: Long = 0
        set(value) {
            field = value
            launch { currentPositionFlow.emit(currentPosition) }
        }

    private val currentPlaylistFlow: MutableStateFlow<List<LSong>> = MutableStateFlow(emptyList())
    private val currentPlayingFlow: MutableStateFlow<LSong?> = MutableStateFlow(null)
    private val currentPositionFlow: MutableStateFlow<Long> = MutableStateFlow(0L)

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
    var currentIsPlaying: Boolean = false
    var currentRepeatMode: Int = 0
    var currentShuffleMode: Int = 0
}