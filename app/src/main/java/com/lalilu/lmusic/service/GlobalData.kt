package com.lalilu.lmusic.service

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.asLiveData
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import com.lalilu.lmusic.datasource.extensions.partCopy
import com.lalilu.lmusic.manager.SearchTextManager
import com.lalilu.lmusic.utils.moveHeadToTailWithSearch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

object GlobalData : CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.IO

    private val handler = Handler(Looper.getMainLooper())
    var getIsPlayingFromPlayer: () -> Boolean = { false }
    var getPositionFromPlayer: () -> Long = { 0L }
        set(value) {
            field = value
            stopUpdate()
            updatePosition()
        }

    fun searchFor(keyword: String?) = launch {
        currentSearchKeyword.emit(keyword)
    }

    private fun stopUpdate() {
        handler.removeCallbacks(this::updatePosition)
    }

    private var lastPlayState = false
    fun updatePosition(force: Boolean = false) {
        if (force) {
            val isPlaying = getIsPlayingFromPlayer()
            val position = getPositionFromPlayer()
            launch {
                currentIsPlaying.emit(isPlaying)
                currentPosition.emit(position)
            }
            return
        }
        val isPlaying = getIsPlayingFromPlayer()
        if (lastPlayState == isPlaying) {
            val position = getPositionFromPlayer()
            launch {
                currentIsPlaying.emit(isPlaying)
                currentPosition.emit(position)
            }
        } else {
            lastPlayState = isPlaying
        }
        handler.postDelayed(this::updatePosition, 100)
    }

    suspend fun updateCurrentMediaItem(targetMediaItemId: String) = withContext(Dispatchers.IO) {
        val mediaItem = currentMediaItem.value
            ?: return@withContext

        if (mediaItem.mediaId == targetMediaItemId) {
            currentMediaItem.emit(mediaItem.partCopy())
        }
    }

    val currentIsPlaying: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val currentPosition: MutableStateFlow<Long> = MutableStateFlow(0L)
    val currentMediaItem: MutableStateFlow<MediaItem?> = MutableStateFlow(null)
    val currentPlaylist: MutableStateFlow<List<MediaItem>> = MutableStateFlow(emptyList())
    private val currentSearchKeyword: MutableStateFlow<String?> = MutableStateFlow(null)

    val currentPlaylistLiveData = currentPlaylist.combine(currentMediaItem) { items, item ->
        item ?: return@combine items
        items.moveHeadToTailWithSearch(item.mediaId) { listItem, id ->
            listItem.mediaId == id
        }
    }.combine(currentSearchKeyword) { items, keyword ->
        SearchTextManager.filter(keyword, items) {
            "${it.mediaMetadata.title} ${it.mediaMetadata.artist}"
        }
    }.asLiveData()

    val currentMediaItemLiveData = currentMediaItem.asLiveData()
    val currentPositionLiveData = currentPosition.asLiveData()

    val playerListener = object : Player.Listener {
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            launch { currentMediaItem.emit(mediaItem) }
            updatePosition(true)
        }
    }
}