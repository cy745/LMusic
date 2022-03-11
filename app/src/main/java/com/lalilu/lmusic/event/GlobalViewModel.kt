package com.lalilu.lmusic.event

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import com.lalilu.lmusic.datasource.extensions.partCopy
import com.lalilu.lmusic.manager.SearchTextUtil
import com.lalilu.lmusic.manager.filter
import com.lalilu.lmusic.utils.moveHeadToTailWithSearch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

@Singleton
class GlobalViewModel @Inject constructor(
    private val searchTextUtil: SearchTextUtil
) : ViewModel(), CoroutineScope {
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

    @UnstableApi
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
        searchTextUtil.filter(keyword, items) {
            "${it.mediaMetadata.title} ${it.mediaMetadata.artist}"
        }
    }.asLiveData()
    val currentMediaItemLiveData: LiveData<MediaItem?> = currentMediaItem.asLiveData()
    val currentPositionLiveData: LiveData<Long> = currentPosition.asLiveData()

    val playerListener = object : Player.Listener {
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            launch { currentMediaItem.emit(mediaItem) }
            updatePosition(true)
        }
    }
}