package com.lalilu.lmusic.manager

import androidx.lifecycle.asLiveData
import androidx.media3.common.Player
import com.dirror.lyricviewx.LyricEntry
import com.dirror.lyricviewx.LyricUtil
import com.lalilu.lmusic.event.GlobalViewModel
import com.lalilu.lmusic.utils.sources.LyricSourceFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

interface LyricPusher {
    fun clearLyric()
    fun pushLyric(sentence: String)
}

@Singleton
@ExperimentalCoroutinesApi
class LyricManager @Inject constructor(
    mGlobal: GlobalViewModel,
    private val pusher: LyricPusher,
    private val lyricSourceFactory: LyricSourceFactory
) : Player.Listener, CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.IO
    private var lastLyric: String? = ""
    private var lastIndex: Int = -1

    private val _songLyric: Flow<Pair<String, String?>?> =
        mGlobal.currentMediaItem.mapLatest {
            it ?: return@mapLatest null
            lyricSourceFactory.getLyric(it)
        }

    val songLyric = _songLyric.asLiveData()

    init {
        mGlobal.currentIsPlaying.onEach { isPlaying ->
            if (!isPlaying) pusher.clearLyric()
        }.launchIn(this)

        _songLyric.mapLatest { pair ->
            if (pair == null) {
                pusher.clearLyric()
                return@mapLatest null
            }
            LyricUtil.parseLrc(arrayOf(pair.first, pair.second))
        }.combine(mGlobal.currentPosition) { list, time ->
            list ?: return@combine null
            if (time == 0L) {
                return@combine null
            }

            val index = findShowLine(list, time + 200)
            val lyricEntry = list.let {
                if (it.isEmpty()) null else it[index]
            }
            val nowLyric = lyricEntry?.text ?: lyricEntry?.secondText
            if (nowLyric == lastLyric && index == lastIndex)
                return@combine null

            lastIndex = index
            lastLyric = nowLyric
            nowLyric
        }.onEach {
            it ?: return@onEach
            pusher.pushLyric(it)
        }.launchIn(this)
    }

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