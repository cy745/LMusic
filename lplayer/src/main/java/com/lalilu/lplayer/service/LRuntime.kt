package com.lalilu.lplayer.service

import android.net.Uri
import com.lalilu.common.base.Playable
import com.lalilu.lplayer.playback.BaseQueue
import com.lalilu.lplayer.playback.UpdatableQueue
import com.lalilu.lplayer.runtime.ItemSource
import com.lalilu.lplayer.runtime.Runtime
import com.lalilu.lplayer.runtime.RuntimeInfo
import kotlinx.coroutines.flow.MutableStateFlow

class LRuntime internal constructor(
    isListLooping: () -> Boolean = { false },
) : Runtime<Playable> {
    private val sourceFlow = MutableStateFlow<ItemSource<Playable>?>(null)

    override var source: ItemSource<Playable>? = null
        set(value) {
            field = value
            sourceFlow.tryEmit(value)
        }
    override val info: RuntimeInfo<Playable> by lazy {
        RuntimeInfo(source = sourceFlow)
    }
    override val queue: UpdatableQueue<Playable> by lazy {
        RuntimeQueueWithInfo(
            info = info, source = { source },
            isListLoopingFunc = isListLooping
        )
    }

    private class RuntimeQueueWithInfo(
        private val info: RuntimeInfo<Playable>,
        private val source: () -> ItemSource<Playable>?,
        private val isListLoopingFunc: () -> Boolean = { true },
    ) : BaseQueue<Playable>() {

        override fun isListLooping(): Boolean = isListLoopingFunc()
        override fun getIds(): List<String> = info.idsFlow.value
        override fun getCurrentId(): String? = info.playingIdFlow.value
        override fun getUriFromItem(item: Playable): Uri = item.targetUri
        override fun getById(id: String): Playable? = source()?.getById(id)
        override fun setIds(ids: List<String>) {
            info.idsFlow.value = ids
        }

        override fun setCurrentId(id: String?) {
            info.playingIdFlow.value = id
        }
    }
}
