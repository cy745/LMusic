package com.lalilu.lmusic.service.runtime

import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmedia.entity.MusicParent
import com.lalilu.lmusic.utils.extension.getNextOf
import com.lalilu.lmusic.utils.extension.getPreviousOf
import com.lalilu.lmusic.utils.extension.move
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.Timer
import kotlin.concurrent.schedule

interface Runtime {
    val _songsFlow: MutableStateFlow<List<LSong>>
    val _playingFlow: MutableStateFlow<LSong?>
    val _positionFlow: MutableStateFlow<Long>
    val _isPlayingFlow: MutableStateFlow<Boolean>
    val _parentFlow: MutableStateFlow<MusicParent?>
    var listener: Listener?
    var timer: Timer?

    /**
     * 加载指定的歌曲进播放列表，更新当前正在播放的歌曲
     */
    fun load(
        parent: MusicParent? = null,
        songs: List<LSong> = parent?.songs ?: emptyList(),
        playing: LSong?
    ) {
        _parentFlow.value = parent
        update(songs)
        if (songs.contains(playing)) {
            update(playing = playing)
        }
    }

    /**
     * 更新当前正在播放的歌曲
     */
    fun update(playing: LSong? = null) {
        listener?.onPlayingUpdate(playing)
        _playingFlow.value = playing
    }

    fun update(songs: List<LSong>) {
        listener?.onSongsUpdate(songs)
        _songsFlow.value = songs
    }

    fun move(from: Int, to: Int) {
        _songsFlow.value = _songsFlow.value.move(from, to)
    }

    fun add(index: Int = -1, song: LSong) {
        _songsFlow.value = _songsFlow.value.toMutableList()
            .apply {
                if (index == -1) {
                    add(song)
                } else {
                    add(index, song)
                }
            }
    }

    fun remove(song: LSong? = null) {
        if (song == null) return
        remove(mediaId = song.id)
    }

    fun remove(mediaId: String) {
        removeAt(indexOfSong(mediaId))
    }

    fun removeAt(index: Int) {
        _songsFlow.value = _songsFlow.value
            .toMutableList()
            .apply { removeAt(index) }
    }

    fun isEmpty(): Boolean = _songsFlow.value.isEmpty()
    fun getPlaying(): LSong? = _playingFlow.value
    fun getPlayingId(): String? = _playingFlow.value?.id
    fun getPlayingIndex(): Int = _songsFlow.value.indexOf(_playingFlow.value)
    fun getSongById(mediaId: String): LSong? = _songsFlow.value.find { it.id == mediaId }
    fun indexOfSong(mediaId: String): Int = _songsFlow.value.indexOfFirst { it.id == mediaId }
    fun getNextOf(song: LSong?, cycle: Boolean): LSong? = _songsFlow.value.getNextOf(song, cycle)
    fun getPreviousOf(song: LSong?, cycle: Boolean): LSong? =
        _songsFlow.value.getPreviousOf(song, cycle)

    fun updatePosition(startPosition: Long, loopDelay: Long = 0) {
        timer?.cancel()
        _positionFlow.value = startPosition
        listener?.onPositionUpdate(_positionFlow.value)

        if (loopDelay <= 0) return
        timer = Timer().apply {
            schedule(0, loopDelay) {
                _positionFlow.value += loopDelay
                listener?.onPositionUpdate(_positionFlow.value)
            }
        }
    }

    interface Listener {
        fun onPlayingUpdate(song: LSong?) {}
        fun onSongsUpdate(songs: List<LSong>) {}
        fun onPositionUpdate(position: Long) {}
        fun onIsPlayingUpdate(isPlaying: Boolean) {}
    }
}