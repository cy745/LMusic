package com.lalilu.lplayer.service

import android.net.Uri
import com.lalilu.common.base.Playable
import com.lalilu.lplayer.playback.BaseQueue
import com.lalilu.lplayer.playback.UpdatableQueue
import com.lalilu.lplayer.runtime.ItemSource
import com.lalilu.lplayer.runtime.Runtime
import com.lalilu.lplayer.runtime.RuntimeInfo

class LRuntime internal constructor(
    isListLooping: () -> Boolean = { false },
) : Runtime<Playable> {
    override var source: ItemSource<Playable>? = null
    override val info: RuntimeInfo by lazy { RuntimeInfo() }
    override val queue: UpdatableQueue<Playable> by lazy {
        RuntimeQueueWithInfo(
            info = info, source = { source },
            isListLoopingFunc = isListLooping
        )
    }

    private class RuntimeQueueWithInfo(
        private val info: RuntimeInfo,
        private val source: () -> ItemSource<Playable>?,
        private val isListLoopingFunc: () -> Boolean = { true }
    ) : BaseQueue<Playable>() {

        override fun isListLooping(): Boolean = isListLoopingFunc()
        override fun getIds(): List<String> = info.itemsFlow.value
        override fun getCurrentId(): String? = info.playingIdFlow.value
        override fun getUriFromItem(item: Playable): Uri = item.targetUri
        override fun getById(id: String): Playable? = source()?.getById(id)
        override fun setIds(ids: List<String>) {
            info.itemsFlow.value = ids
        }

        override fun setCurrentId(id: String?) {
            info.playingIdFlow.value = id
        }
    }
}
