package com.lalilu.lmusic.repository

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.dirror.lyricviewx.LyricUtil
import com.lalilu.lmedia.LMedia
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmusic.service.LMusicRuntime
import com.lalilu.lmusic.utils.extension.findShowLine
import com.lalilu.lmedia.repository.LyricSourceFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class LyricRepository(
    private val lyricSource: LyricSourceFactory,
    private val runtime: LMusicRuntime
) : CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.IO

    @Composable
    fun rememberHasLyric(song: LSong): State<Boolean> {
        return remember { mutableStateOf(false) }.also { state ->
            LaunchedEffect(song) {
                if (isActive) {
                    state.value = hasLyric(song)
                }
            }
        }
    }

    suspend fun hasLyric(song: LSong): Boolean = withContext(Dispatchers.IO) {
        getLyric(song) != null
    }

    suspend fun getLyric(song: LSong): Pair<String, String?>? = withContext(Dispatchers.IO) {
        return@withContext lyricSource.loadLyric(song)
            ?.takeIf { it.first.isNotEmpty() }
    }

    val currentLyric: Flow<Pair<String, String?>?> =
        runtime.playingFlow.flatMapLatest { item ->
            LMedia.getSongFlowById(item?.id)
                .mapLatest { it?.let { getLyric(it) } }
        }

    val currentLyricSentence: Flow<String?> = currentLyric.mapLatest { pair ->
        pair ?: return@mapLatest null
        LyricUtil.parseLrc(arrayOf(pair.first, pair.second))
    }.flatMapLatest { lyrics ->
        runtime.positionFlow.mapLatest {
            findShowLine(lyrics, it + 500)
        }.distinctUntilChanged()
            .mapLatest { lyrics?.getOrNull(it)?.text }
    }.debounce(100)
}