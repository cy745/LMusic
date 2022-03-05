package com.lalilu.lmusic.manager

import android.os.Handler
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import com.dirror.lyricviewx.LyricEntry
import com.dirror.lyricviewx.LyricUtil
import com.lalilu.lmusic.datasource.extensions.getSongData
import com.lalilu.lmusic.utils.EmbeddedDataUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

@ExperimentalCoroutinesApi
class LyricManager(
    private val pusher: LyricPusher
) : Player.Listener, CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.IO
    var positionGet: () -> Long = { 0L }
        set(value) {
            field = value
            updatePosition()
        }

    interface LyricPusher {
        fun clearLyric()
        fun pushLyric(sentence: String)
    }

    val playerListener = object : Player.Listener {
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            launch { _currentMediaItemFlow.emit(mediaItem) }
            updatePosition(true)
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            if (isPlaying) {
                updatePosition(true)
            } else {
                pusher.clearLyric()
            }
        }
    }

    private fun updatePosition(force: Boolean = false) {
        val position = positionGet.invoke()
        launch { _currentPositionFlow.emit(position) }
        if (!force) Handler().postDelayed(this::updatePosition, 100)
    }

    private val _currentPositionFlow: MutableStateFlow<Long> = MutableStateFlow(0)
    private val _currentMediaItemFlow: MutableStateFlow<MediaItem?> = MutableStateFlow(null)

    private var lastLyric: String? = ""
    private var lastIndex: Int = 0

    private val _songLyrics = _currentMediaItemFlow.mapLatest {
        pusher.clearLyric()
        it ?: return@mapLatest null

        LyricUtil.parseLrc(
            arrayOf(
                EmbeddedDataUtils.loadLyric(it.mediaMetadata.getSongData()), null
            )
        )
    }.combine(_currentPositionFlow) { list, time ->
        val index = findShowLine(list, time)
        val lyricEntry = list?.let {
            if (it.isEmpty()) null else it[index]
        }
        val nowLyric = lyricEntry?.text ?: lyricEntry?.secondText
        if (nowLyric == lastLyric && index == lastIndex)
            return@combine null

        lastIndex = index
        lastLyric = nowLyric
        nowLyric
    }.flowOn(Dispatchers.IO)
        .onEach {
            it ?: return@onEach
            pusher.pushLyric(it)
        }.launchIn(this)

    private fun findShowLine(list: List<LyricEntry>?, time: Long): Int {
        if (list == null || list.isEmpty()) return 0
        var left = 0
        var right = list.size
        while (left <= right) {
            val middle = (left + right) / 2
            val middleTime = list[middle].time
            if (time < middleTime) {
                right = middle - 1
            } else {
                if (middle + 1 >= list.size || time < list[middle + 1].time) {
                    return middle
                }
                left = middle + 1
            }
        }
        return 0
    }
}