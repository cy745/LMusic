package com.lalilu.lplayer.runtime

import com.lalilu.lplayer.playback.UpdatableQueue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import java.util.Timer
import kotlin.concurrent.schedule

interface Runtime<T> {
    val info: RuntimeInfo<T>
    val queue: UpdatableQueue<T>
    var source: ItemSource<T>?
}

@OptIn(ExperimentalCoroutinesApi::class)
class RuntimeInfo<T>(source: Flow<ItemSource<T>?>) {
    val playingIdFlow: MutableStateFlow<String?> = MutableStateFlow(null)
    val idsFlow: MutableStateFlow<List<String>> = MutableStateFlow(emptyList())

    val playingFlow: Flow<T?> = source.flatMapLatest {
        it?.flowMapId(playingIdFlow) ?: flowOf(null)
    }
    val listFlow: Flow<List<T>> = source.flatMapLatest {
        it?.flowMapIds(idsFlow) ?: flowOf(emptyList())
    }

    val isPlayingFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val positionFlow: MutableStateFlow<Long> = MutableStateFlow(0)
    val durationFlow: MutableStateFlow<Long> = MutableStateFlow(0)
    val bufferedPositionFlow: MutableStateFlow<Long> = MutableStateFlow(0)

    var getPosition: () -> Long = { 0L }
    var getDuration: () -> Long = { 0L }
    var getBufferedPosition: () -> Long = { 0L }
    private var timer: Timer? = null

    fun updateIsPlaying(isPlaying: Boolean) {
        isPlayingFlow.value = isPlaying
    }

    fun updatePosition(startPosition: Long, isPlaying: Boolean) {
        timer?.cancel()
        positionFlow.value = startPosition

        if (!isPlaying) return
        timer = Timer().apply {
            schedule(0, 50L) {
                positionFlow.value = getPosition()
                durationFlow.value = getDuration()
                bufferedPositionFlow.value = getBufferedPosition()
            }
        }
    }
}

interface ItemSource<T> {
    fun getById(id: String): T?
    fun flowMapId(idFlow: Flow<String?>): Flow<T?>
    fun flowMapIds(idsFlow: Flow<List<String>>): Flow<List<T>>
}