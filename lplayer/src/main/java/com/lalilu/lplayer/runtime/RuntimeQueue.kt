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

    override fun moveToNext(item: LSong) {
        val newSongs = runtime.songsIdsFlow.value.toMutableList()

        val targetItemIndex = newSongs.indexOf(item.id)
        if (targetItemIndex != -1) {
            newSongs.removeAt(targetItemIndex)
        }

        val playingIndex = runtime.getPlayingId()
            ?.let { newSongs.indexOf(it) }
            ?.takeIf { it >= 0 }
            ?: 0

        // TODO 待验证
        if (playingIndex + 1 >= newSongs.size) {
            newSongs.add(item.id)
        } else {
            newSongs.add(playingIndex + 1, item.id)
        }
        runtime.update(songs = newSongs)
    }

    override fun moveToPrevious(item: LSong) {
        val newSongs = runtime.songsIdsFlow.value.toMutableList()

        val targetItemIndex = newSongs.indexOf(item.id)
        if (targetItemIndex != -1) {
            newSongs.removeAt(targetItemIndex)
        }

        val playingIndex = runtime.getPlayingId()
            ?.let { newSongs.indexOf(it) }
            ?.takeIf { it >= 0 }
            ?: 0

        newSongs.add(playingIndex, item.id)
        runtime.update(songs = newSongs)
    }
}