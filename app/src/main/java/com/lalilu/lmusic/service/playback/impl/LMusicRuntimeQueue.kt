package com.lalilu.lmusic.service.playback.impl

import android.net.Uri
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmusic.service.playback.PlayQueue
import com.lalilu.lmusic.service.runtime.LMusicRuntime

class LMusicRuntimeQueue(
    private val runtime: LMusicRuntime
) : PlayQueue<LSong> {
    private var tempList: List<String>? = null

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

    override fun getById(id: String): LSong? {
        return runtime.getItemById(id)
    }

    override fun getUriFromItem(item: LSong): Uri {
        return item.uri
    }

    override fun setCurrent(item: LSong) {
        runtime.update(playing = item.id)
    }

    override fun shuffle() {
        if (runtime.songsIdsFlow.value.isNotEmpty()) {
            tempList = runtime.songsIdsFlow.value
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