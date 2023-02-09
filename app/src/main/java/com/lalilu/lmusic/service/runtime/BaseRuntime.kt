package com.lalilu.lmusic.service.runtime

import androidx.lifecycle.asLiveData
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmedia.entity.MusicParent
import com.lalilu.lmusic.utils.extension.getNextOf
import com.lalilu.lmusic.utils.extension.moveHeadToTailWithSearch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import java.util.LinkedList
import java.util.Timer

abstract class BaseRuntime : Runtime {

    private val shuffleHistory: LinkedList<LSong> = LinkedList()
    final override val _songsFlow: MutableStateFlow<List<LSong>> = MutableStateFlow(emptyList())
    final override val _playingFlow: MutableStateFlow<LSong?> = MutableStateFlow(null)
    final override val _positionFlow: MutableStateFlow<Long> = MutableStateFlow(0L)
    final override val _isPlayingFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
    final override val _parentFlow: MutableStateFlow<MusicParent?> = MutableStateFlow(null)
    override var listener: Runtime.Listener? = null
    override var timer: Timer? = null

    fun getRandomNext(): LSong? {
        if (_songsFlow.value.isEmpty()) return null
        if (_songsFlow.value.size in 1..2) {
            return _songsFlow.value.getNextOf(_playingFlow.value, true)
                ?.also { shuffleHistory.push(it) }
        }

        var result: LSong
        while (true) {
            result = _songsFlow.value.randomOrNull() ?: return null
            if (result.id != _playingFlow.value?.id) {
                break
            }
        }
        shuffleHistory.push(result)
        return result
    }

    fun getRandomPrevious(): LSong? {
        var result: LSong?
        while (true) {
            if (shuffleHistory.isEmpty()) {
                result = null
                break
            }
            result = shuffleHistory.pop()
            if (indexOfSong(result.id) >= 0) {
                break
            }
        }
        return result
    }

    val isPlayingFlow: StateFlow<Boolean> = _isPlayingFlow
    val playingFlow: StateFlow<LSong?> = _playingFlow
    val positionFlow: StateFlow<Long> = _positionFlow

    val playingLiveData = _playingFlow.asLiveData()
    val positionLiveData = _positionFlow.asLiveData()
    val isPlayingLiveData = _isPlayingFlow.asLiveData()
    val songsLiveData = _songsFlow.combine(_playingFlow) { items, item ->
        item ?: return@combine items
        items.moveHeadToTailWithSearch(item.id) { listItem, id -> listItem.id == id }
    }.asLiveData()
}