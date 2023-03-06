package com.lalilu.lmusic.service.runtime

import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmedia.entity.MusicParent
import com.lalilu.lmusic.utils.extension.moveHeadToTailWithSearch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import java.util.Timer

abstract class BaseRuntime : Runtime {

    final override val _songsFlow: MutableStateFlow<List<LSong>> = MutableStateFlow(emptyList())
    final override val _playingFlow: MutableStateFlow<LSong?> = MutableStateFlow(null)
    final override val _positionFlow: MutableStateFlow<Long> = MutableStateFlow(0L)
    final override val _isPlayingFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
    final override val _parentFlow: MutableStateFlow<MusicParent?> = MutableStateFlow(null)
    override var listener: Runtime.Listener? = null
    override var timer: Timer? = null

    val playingFlow: Flow<LSong?> = _playingFlow
    val positionFlow: Flow<Long> = _positionFlow
    val isPlayingFlow: Flow<Boolean> = _isPlayingFlow
    val songsFlow = _songsFlow.combine(_playingFlow) { items, item ->
        item ?: return@combine items
        items.moveHeadToTailWithSearch(item.id) { listItem, id -> listItem.id == id }
    }
}