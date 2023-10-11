package com.lalilu.lplayer.runtime

import com.lalilu.common.base.Playable
import com.lalilu.lplayer.playback.IdBaseQueue
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.Timer
import kotlin.concurrent.schedule

interface Runtime<T> {
    val info: RuntimeInfo
    val queue: IdBaseQueue<T>
    var source: ItemSource<T>?
}

class RuntimeInfo {
    val isPlayingFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val playingIdFlow: MutableStateFlow<String?> = MutableStateFlow(null)
    val itemsFlow: MutableStateFlow<List<String>> = MutableStateFlow(emptyList())
    val positionFlow: MutableStateFlow<Long> = MutableStateFlow(0)
    val durationFlow: MutableStateFlow<Long> = MutableStateFlow(0)
    val bufferedPositionFlow: MutableStateFlow<Long> = MutableStateFlow(0)

    var getPosition: () -> Long = { 0 }
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
            }
        }
    }
}

interface ItemSource<T> {
    fun getById(id: String): T?
}


fun interface PlayableSource : ItemSource<Playable> {
    override fun getById(id: String): Playable?
}