package com.lalilu.lmusic.service.playback.impl

import android.net.Uri
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmusic.service.playback.PlayQueue
import com.lalilu.lmusic.service.runtime.LMusicRuntime

class LMusicRuntimeQueue(
    private val runtime: LMusicRuntime
) : PlayQueue<LSong> {
    private var tempList: List<LSong>? = null

    override fun getCurrent(): LSong? {
        return runtime.getPlaying() ?: runtime._songsFlow.value.getOrNull(0)
    }

    override fun getPrevious(): LSong? {
        return runtime.getPreviousOf(getCurrent(), true)
    }

    override fun getNext(): LSong? {
        return runtime.getNextOf(getCurrent(), true)
    }

    override fun getById(id: String): LSong? {
        return runtime.getSongById(id)
    }

    override fun getUriFromItem(item: LSong): Uri {
        return item.uri
    }

    override fun setCurrent(item: LSong) {
        runtime.update(playing = item)
    }

    override fun shuffle() {
        if (runtime._songsFlow.value.isNotEmpty()) {
            tempList = runtime._songsFlow.value
            runtime.update(tempList!!.shuffled())
        }
    }

    override fun recoverOrder() {
        if (tempList?.isNotEmpty() == true) {
            runtime.update(tempList!!)
            tempList = null
        }
    }
}