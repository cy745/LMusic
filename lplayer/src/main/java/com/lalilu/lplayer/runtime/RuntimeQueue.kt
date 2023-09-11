package com.lalilu.lplayer.runtime

import android.net.Uri
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lplayer.playback.PlayQueue

class RuntimeQueue(
    private val runtime: Runtime<LSong>,
) : PlayQueue<LSong> {

    override fun getCurrent(): LSong? {
        return runtime.getPlaying()
    }

    override fun getPrevious(): LSong? {
        val current = getCurrent() ?: return null
        return runtime.getPreviousOf(current, true)
    }

    override fun getNext(): LSong? {
        val current = getCurrent() ?: return null
        return runtime.getNextOf(current, true)
    }

    override fun getShuffle(): LSong? {
        return runtime.getShuffle()
    }

    override fun getById(id: String): LSong? {
        return runtime.getItemById(id)
    }

    override fun getUriFromItem(item: LSong): Uri {
        return item.uri
    }

    override fun setCurrent(item: LSong) {
        runtime.update(playing = item.id)
    }

    override fun updateQueue() {
        // TODO 随机、去重复
//        if (runtime.songsIdsFlow.value.isNotEmpty()) {
//            runtime.update(runtime.songsIdsFlow.value.shuffled())
//        }
    }
}