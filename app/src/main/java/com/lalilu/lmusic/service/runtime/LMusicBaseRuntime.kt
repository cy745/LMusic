package com.lalilu.lmusic.service.runtime

import androidx.lifecycle.asLiveData
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmedia.entity.MusicParent
import com.lalilu.lmusic.utils.extension.getNextOf
import com.lalilu.lmusic.utils.extension.getPreviousOf
import com.lalilu.lmusic.utils.extension.move
import com.lalilu.lmusic.utils.extension.moveHeadToTailWithSearch
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import java.util.*

abstract class LMusicBaseRuntime : CoroutineScope {
    open suspend fun onPlayingUpdate(song: LSong?) {}
    open suspend fun onSongsUpdate(songs: List<LSong>) {}
    open suspend fun onPositionUpdate(position: Long) {}
    open suspend fun onIsPlayingUpdate(isPlaying: Boolean) {}

    private val shuffleHistory: LinkedList<LSong> = LinkedList()
    private var playlist: MusicParent? = null
    private var songs: List<LSong> = emptyList()
        set(value) {
            field = value
            launch {
                _songsFlow.emit(value)
                onSongsUpdate(value)
            }
        }
    private var playing: LSong? = null
        set(value) {
            field = value
            launch {
                _playingFlow.emit(value)
                onPlayingUpdate(value)
            }
        }
    private var position: Long = 0L
        set(value) {
            field = value
            launch {
                _positionFlow.emit(value)
                onPositionUpdate(value)
            }
        }
    var isPlaying: Boolean = false
        set(value) {
            field = value
            launch {
                _isPlayingFlow.emit(value)
                onIsPlayingUpdate(value)
            }
        }

    fun load(
        songs: List<LSong> = emptyList(),
        playlist: MusicParent? = null,
        playing: LSong? = null
    ) {
        this.songs = playlist?.songs ?: songs
        this.playlist = playlist
        this.playing = playing?.takeIf { songs.contains(it) }
    }

    fun isEmpty(): Boolean = songs.isEmpty()
    fun getPlaying(): LSong? = playing
    fun getPlayingId(): String? = playing?.id
    fun getPlayingIndex(): Int = songs.indexOf(playing)
    fun getSongById(mediaId: String): LSong? = songs.find { it.id == mediaId }
    fun indexOfSong(mediaId: String): Int = songs.indexOfFirst { it.id == mediaId }
    fun getNextOf(song: LSong?, cycle: Boolean): LSong? = songs.getNextOf(song, cycle)
    fun getPreviousOf(song: LSong?, cycle: Boolean): LSong? = songs.getPreviousOf(song, cycle)

    fun getRandomNext(): LSong? {
        if (songs.isEmpty()) return null
        if (songs.size in 1..2) return songs.getNextOf(playing, true)?.also {
            shuffleHistory.push(it)
        }

        var result: LSong
        var index: Int
        while (true) {
            result = songs.randomOrNull() ?: return null
            index = shuffleHistory.indexOf(result)
            if (result.id != playing?.id) {
                break
            }
        }
        shuffleHistory.push(result)
        return result
    }

    fun getRandomPrevious(): LSong? {
        var result: LSong?
        while (true) {
            if (shuffleHistory.isEmpty()) {
                result = null
                break
            }
            result = shuffleHistory.pop()
            if (indexOfSong(result.id) >= 0) {
                break
            }
        }
        return result
    }

    fun move(from: Int, to: Int) {
        songs = songs.move(from, to)
    }

    fun add(index: Int, song: LSong) {
        songs = songs.toMutableList().apply { add(index, song) }
    }

    fun updatePlaying(song: LSong?) {
        this.playing = song
    }

    private var lastRemovedIndex: Int = -1
    private var lastRemovedItem: LSong? = null

    fun remove(mediaId: String, song: LSong? = null) {
        val index = indexOfSong(song?.id ?: mediaId)
        removeAt(index)
    }

    fun removeAt(index: Int) {
        lastRemovedIndex = index
        songs = songs.toMutableList().apply { lastRemovedItem = removeAt(lastRemovedIndex) }
    }

    private val _songsFlow = MutableStateFlow(songs)
    private val _playingFlow = MutableStateFlow(playing)
    private val _positionFlow = MutableStateFlow(position)
    private val _isPlayingFlow = MutableStateFlow(isPlaying)

    val playingFlow: StateFlow<LSong?> = _playingFlow
    val playingLiveData = _playingFlow.asLiveData()
    val positionLiveData = _positionFlow.asLiveData()
    val isPlayingLiveData = _isPlayingFlow.asLiveData()
    val songsLiveData = _songsFlow.combine(_playingFlow) { items, item ->
        item ?: return@combine items
        items.moveHeadToTailWithSearch(item.id) { listItem, id -> listItem.id == id }
    }.asLiveData()

    private var updatePositionJob: Job? = null
    fun updatePosition(startPosition: Long, loopDelay: Long = 0) {
        position = startPosition
        updatePositionJob?.cancel()

        if (loopDelay <= 0) return
        updatePositionJob = launch(Dispatchers.Default) {
            while (isActive) {
                delay(loopDelay)
                position += loopDelay
            }
        }
    }
}