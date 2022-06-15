package com.lalilu.lmusic.manager

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.asLiveData
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import com.lalilu.lmusic.datasource.MDataBase
import com.lalilu.lmusic.utils.moveHeadToTailWithSearch
import com.lalilu.lmusic.utils.safeLaunch
import com.lalilu.lmusic.utils.updateArtworkUri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

@Singleton
@OptIn(ExperimentalCoroutinesApi::class)
class GlobalDataManager @Inject constructor(
    private val dataBase: MDataBase,
    private val searchTextManager: SearchTextManager
) : CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.IO

    private val handler = Handler(Looper.getMainLooper())
    var getIsPlayingFromPlayer: () -> Boolean = { false }
    var getPositionFromPlayer: () -> Long = { 0L }
        set(value) {
            field = value
            stopUpdate()
            updatePositionLoop()
        }

    fun searchFor(keyword: String?) = safeLaunch {
        currentSearchKeyword.emit(keyword)
    }

    private fun stopUpdate() {
        handler.removeCallbacks(this::updatePositionLoop)
    }

    private var lastPlayState = false
    private fun updatePositionLoop() {
        val isPlaying = getIsPlayingFromPlayer()
        val position = getPositionFromPlayer()

        if (lastPlayState == isPlaying && isPlaying) {
            updatePosition(isPlaying, position)
        } else {
            lastPlayState = isPlaying
        }
        handler.postDelayed(this::updatePositionLoop, 100)
    }

    fun updatePosition(
        isPlaying: Boolean = getIsPlayingFromPlayer(),
        position: Long = getPositionFromPlayer()
    ) {
        safeLaunch {
            HistoryManager.lastPlayedPosition = position
            currentIsPlaying.emit(isPlaying)
            currentPosition.emit(position)
        }
    }

    val currentIsPlaying: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val currentPosition: MutableStateFlow<Long> = MutableStateFlow(0L)
    val currentMediaItem: MutableStateFlow<MediaItem?> = MutableStateFlow(null)
    val currentPlaylist: MutableStateFlow<List<MediaItem>> = MutableStateFlow(emptyList())
    private val currentSearchKeyword: MutableStateFlow<String?> = MutableStateFlow(null)

    private val currentPlaylistFlow = currentPlaylist.combine(currentMediaItem) { items, item ->
        item ?: return@combine items
        items.moveHeadToTailWithSearch(item.mediaId) { listItem, id ->
            listItem.mediaId == id
        }
    }.combine(currentSearchKeyword) { items, keyword ->
        searchTextManager.filter(keyword, items) {
            "${it.mediaMetadata.title} ${it.mediaMetadata.artist}"
        }
    }

    val currentMediaItemFlow = currentMediaItem.flatMapLatest { mediaItem ->
        dataBase.networkDataDao().getFlowById(mediaItem?.mediaId).mapLatest {
            return@mapLatest mediaItem?.updateArtworkUri(it?.requireCoverUri())
        }
    }.flowOn(Dispatchers.IO)

    val currentMediaItemLiveData = currentMediaItemFlow.asLiveData()
    val currentPositionLiveData = currentPosition.asLiveData()
    val currentPlaylistLiveData = currentPlaylistFlow.asLiveData()

    val playerListener = object : Player.Listener {
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            safeLaunch { currentMediaItem.emit(mediaItem) }
            updatePosition()
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            updatePosition()
        }
    }
}