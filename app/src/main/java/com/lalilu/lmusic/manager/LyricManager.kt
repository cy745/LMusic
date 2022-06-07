package com.lalilu.lmusic.manager

import androidx.lifecycle.asLiveData
import androidx.media3.common.Player
import com.dirror.lyricviewx.LyricEntry
import com.dirror.lyricviewx.LyricUtil
import com.lalilu.lmusic.utils.sources.LyricSourceFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapLatest
import kotlin.coroutines.CoroutineContext

/**
 * 专门负责歌词解析处理的全局单例
 */
@OptIn(ExperimentalCoroutinesApi::class)
object LyricManager : Player.Listener, CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.IO
    var lyricSourceFactory: LyricSourceFactory? = null

    private val currentLyric: Flow<Pair<String, String?>?> =
        GlobalDataManager.currentMediaItem.mapLatest {
            it ?: return@mapLatest null
            lyricSourceFactory?.getLyric(it)
        }

    val currentSentence: Flow<String?> =
        currentLyric.mapLatest { pair ->
            pair ?: return@mapLatest null
            LyricUtil.parseLrc(arrayOf(pair.first, pair.second))
        }.combine(GlobalDataManager.currentPosition) { list, time ->
            if (list == null || time == 0L) return@combine null

            val index = findShowLine(list, time + 200)
            val lyricEntry = list.getOrNull(index)
            val nowLyric = lyricEntry?.text ?: lyricEntry?.secondText

            return@combine nowLyric to index
        }.distinctUntilChanged()
            .combine(GlobalDataManager.currentIsPlaying) { pair, isPlaying ->
                if (pair == null || !isPlaying) return@combine null
                return@combine pair.first
            }

    val currentLyricLiveData = currentLyric.asLiveData()

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