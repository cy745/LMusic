package com.lalilu.lplayer.runtime

import android.net.Uri
import com.lalilu.common.base.Playable
import com.lalilu.lplayer.playback.PlayQueue

class RuntimeQueue(
    private val runtime: Runtime<Playable>,
) : PlayQueue<Playable> {

    override fun getCurrent(): Playable? {
        return runtime.getPlaying()
    }

    override fun getPrevious(): Playable? {
        val current = getCurrent() ?: return null
        return runtime.getPreviousOf(current, true)
    }

    override fun getNext(): Playable? {
        val current = getCurrent() ?: return null
        return runtime.getNextOf(current, true)
    }

    override fun getShuffle(): Playable? {
        return runtime.getShuffle()
    }

    override fun getById(id: String): Playable? {
        return runtime.getItemById(id)
    }

    override fun getUriFromItem(item: Playable): Uri {
        return item.targetUri
    }

    override fun setCurrent(item: Playable) {
        runtime.update(playing = item.mediaId)
    }

    override fun moveToNext(item: Playable) {
        val newSongs = runtime.songsIdsFlow.value.toMutableList()

        val targetItemIndex = newSongs.indexOf(item.mediaId)
        if (targetItemIndex != -1) {
            newSongs.removeAt(targetItemIndex)
        }

        val playingIndex = runtime.getPlayingId()
            ?.let { newSongs.indexOf(it) }
            ?.takeIf { it >= 0 }
            ?: 0

        // TODO 待验证
        if (playingIndex + 1 >= newSongs.size) {
            newSongs.add(item.mediaId)
        } else {
            newSongs.add(playingIndex + 1, item.mediaId)
        }
        runtime.update(songs = newSongs)
    }

    override fun moveToPrevious(item: Playable) {
        val newSongs = runtime.songsIdsFlow.value.toMutableList()

        val targetItemIndex = newSongs.indexOf(item.mediaId)
        if (targetItemIndex != -1) {
            newSongs.removeAt(targetItemIndex)
        }

        val playingIndex = runtime.getPlayingId()
            ?.let { newSongs.indexOf(it) }
            ?.takeIf { it >= 0 }
            ?: 0

        newSongs.add(playingIndex, item.mediaId)
        runtime.update(songs = newSongs)
    }
}