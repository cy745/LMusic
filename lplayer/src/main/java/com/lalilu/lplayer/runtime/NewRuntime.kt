package com.lalilu.lplayer.runtime

import android.net.Uri
import com.lalilu.common.base.Playable

object NewRuntime : Runtime<Playable> {
    override var source: ItemSource<Playable>? = null
    override val info: RuntimeInfo = RuntimeInfo()
    override val queue: IdBaseQueue<Playable> = RuntimeQueueWithInfo(info) { source }

    private class RuntimeQueueWithInfo(
        private val info: RuntimeInfo,
        private val source: () -> ItemSource<Playable>?
    ) : IdBaseQueue<Playable>() {
        override var playingId: String?
            get() = info.playingIdFlow.value
            set(value) {
                info.playingIdFlow.value = value
            }
        override var items: List<String>
            get() = info.itemsFlow.value
            set(value) {
                info.itemsFlow.value = value
            }

        override fun getIdFromItem(item: Playable): String = item.mediaId
        override fun getUriFromItem(item: Playable): Uri = item.targetUri
        override fun getById(id: String): Playable? = source()?.getById(id)
    }
}
