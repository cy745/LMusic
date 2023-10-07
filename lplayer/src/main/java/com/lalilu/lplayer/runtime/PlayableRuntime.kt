package com.lalilu.lplayer.runtime

import com.lalilu.common.base.Playable
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.Timer

abstract class PlayableRuntime : Runtime<Playable> {
    override val songsIdsFlow: MutableStateFlow<List<String>> = MutableStateFlow(emptyList())
    override val playingIdFlow: MutableStateFlow<String?> = MutableStateFlow(null)
    override val positionFlow: MutableStateFlow<Long> = MutableStateFlow(0L)
    override val isPlayingFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
    override var getPosition: () -> Long = { 0L }
    override var listener: Runtime.Listener? = null
    override var timer: Timer? = null

    override fun getPlaying(): Playable? {
        return getItemById(playingIdFlow.value)
    }

    override fun getPreviousOf(item: Playable, cycle: Boolean): Playable? {
        val previousId = getPreviousOf(item.mediaId, cycle) ?: return null
        return getItemById(previousId)
    }

    override fun getNextOf(item: Playable, cycle: Boolean): Playable? {
        val nextId = getNextOf(item.mediaId, cycle) ?: return null
        return getItemById(nextId)
    }
}