package com.lalilu.lplayer.runtime

import kotlinx.coroutines.flow.MutableStateFlow
import java.util.Timer
import kotlin.concurrent.schedule

interface Runtime<T> {
    val songsIdsFlow: MutableStateFlow<List<String>>
    val playingIdFlow: MutableStateFlow<String?>
    val positionFlow: MutableStateFlow<Long>
    val isPlayingFlow: MutableStateFlow<Boolean>

    var listener: Listener?
    var timer: Timer?

    fun getPlaying(): T?
    fun getItemById(mediaId: String?): T?
    fun getPreviousOf(item: T, cycle: Boolean): T?
    fun getNextOf(item: T, cycle: Boolean): T?


    /**
     * 加载指定的歌曲进播放列表，更新当前正在播放的歌曲
     */
    fun load(
        songs: List<String> = emptyList(),
        playing: String?
    ) {
        update(songs)
        if (songs.contains(playing)) {
            update(playing = playing)
        }
    }

    /**
     * 更新当前正在播放的歌曲
     */
    fun update(playing: String? = null) {
        listener?.onPlayingUpdate(playing)
        playingIdFlow.value = playing
    }

    fun update(songs: List<String>) {
        listener?.onSongsUpdate(songs)
        songsIdsFlow.value = songs
    }

    fun update(isPlaying: Boolean) {
        listener?.onIsPlayingUpdate(isPlaying)
        isPlayingFlow.value = isPlaying
    }

    fun move(from: Int, to: Int) {
        update(songsIdsFlow.value.move(from, to))
    }

    fun add(index: Int = -1, song: String) {
        update(songsIdsFlow.value.add(index, song))
    }

    fun remove(mediaId: String) {
        removeAt(indexOfSong(mediaId))
    }

    fun removeAt(index: Int) {
        update(songsIdsFlow.value.removeAt(index))
    }

    fun isEmpty(): Boolean = songsIdsFlow.value.isEmpty()
    fun getPlayingId(): String? = playingIdFlow.value
    fun getPlayingIndex(): Int = songsIdsFlow.value.indexOf(playingIdFlow.value)
    fun indexOfSong(mediaId: String): Int = songsIdsFlow.value.indexOfFirst { it == mediaId }
    fun getNextOf(mediaId: String?, cycle: Boolean): String? =
        songsIdsFlow.value.getNextOf(mediaId, cycle)

    fun getPreviousOf(mediaId: String?, cycle: Boolean): String? =
        songsIdsFlow.value.getPreviousOf(mediaId, cycle)

    fun updatePosition(startPosition: Long, loopDelay: Long = 0) {
        timer?.cancel()
        positionFlow.value = startPosition
        listener?.onPositionUpdate(positionFlow.value)

        if (loopDelay <= 0) return
        timer = Timer().apply {
            schedule(0, loopDelay) {
                positionFlow.value += loopDelay
                listener?.onPositionUpdate(positionFlow.value)
            }
        }
    }

    interface Listener {
        fun onPlayingUpdate(songId: String?) {}
        fun onSongsUpdate(songsIds: List<String>) {}
        fun onPositionUpdate(position: Long) {}
        fun onIsPlayingUpdate(isPlaying: Boolean) {}
    }

    fun <T> List<T>.getNextOf(item: T, cycle: Boolean = false): T? {
        val nextIndex = indexOf(item) + 1
        return getOrNull(if (cycle) nextIndex % size else nextIndex)
    }


    fun <T> List<T>.getPreviousOf(item: T, cycle: Boolean = false): T? {
        var previousIndex = indexOf(item) - 1
        if (previousIndex < 0 && cycle) {
            previousIndex = size - 1
        }
        return getOrNull(previousIndex)
    }

    fun <T : Any> List<T>.move(from: Int, to: Int): List<T> = toMutableList().apply {
        val targetIndex = if (from < to) to else to + 1
        val temp = removeAt(from)
        add(targetIndex, temp)
    }

    fun <T : Any> List<T>.add(index: Int = -1, item: T): List<T> = toMutableList().apply {
        if (index == -1) add(item) else add(index, item)
    }

    fun <T : Any> List<T>.removeAt(index: Int): List<T> = toMutableList().apply {
        removeAt(index)
    }
}