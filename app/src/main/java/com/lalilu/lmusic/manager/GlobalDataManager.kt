package com.lalilu.lmusic.manager

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.asLiveData
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Player.*
import androidx.media3.common.util.Util
import com.lalilu.lmusic.datasource.MDataBase
import com.lalilu.lmusic.utils.moveHeadToTailWithSearch
import com.lalilu.lmusic.utils.safeLaunch
import com.lalilu.lmusic.utils.then
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

    companion object {
        const val MAX_UPDATE_INTERVAL_MS = 1000L
        const val DEFAULT_TIME_BAR_MIN_UPDATE_INTERVAL_MS = 200L
    }

    var player: Player? = null
    private var currentWindowOffset: Long = 0
    private var currentPosition: Long = 0
    private var timeBarMinUpdateIntervalMs: Long = DEFAULT_TIME_BAR_MIN_UPDATE_INTERVAL_MS

    private fun updatePositionLoop() {
        var position: Long = 0
        if (player != null) {
            position = currentWindowOffset + player!!.contentPosition
        }
        val positionChanged = position != currentPosition
        currentPosition = position

        if (positionChanged) {
            updatePosition(player?.isPlaying == true, position)
        }

        val playbackState = if (player == null) STATE_IDLE else player!!.playbackState
        if (player != null && player!!.isPlaying) {
            var mediaTimeDelayMs = 1000L
            val mediaTimeUntilNextFullSecondMs = 1000 - position % 1000
            val playbackSpeed = player!!.playbackParameters.speed

            mediaTimeDelayMs = mediaTimeDelayMs.coerceAtMost(mediaTimeUntilNextFullSecondMs)
            var delayMs = if (playbackSpeed > 0) {
                (mediaTimeDelayMs / playbackSpeed).toLong()
            } else 1000

            delayMs = Util.constrainValue(
                delayMs, timeBarMinUpdateIntervalMs, MAX_UPDATE_INTERVAL_MS
            )
            handler.postDelayed(this::updatePositionLoop, delayMs)
        } else if (playbackState != STATE_ENDED && playbackState != STATE_IDLE) {
            handler.postDelayed(this::updatePositionLoop, MAX_UPDATE_INTERVAL_MS)
        }
    }

    private fun updatePosition(
        isPlaying: Boolean,
        position: Long
    ) {
        safeLaunch {
            HistoryManager.lastPlayedPosition = position
            currentIsPlayingFlow.emit(isPlaying)
            currentPositionFlow.emit(position)
        }
    }

    fun searchFor(keyword: String?) = safeLaunch {
        currentSearchKeyword.emit(keyword)
    }

    val currentIsPlayingFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val currentPositionFlow: MutableStateFlow<Long> = MutableStateFlow(0L)
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
    val currentPositionLiveData = currentPositionFlow.asLiveData()
    val currentPlaylistLiveData = currentPlaylistFlow.asLiveData()

    val playerListener = object : Listener {
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            HistoryManager.lastPlayedId = mediaItem?.mediaId
            safeLaunch { currentMediaItem.emit(mediaItem) }
        }

        override fun onEvents(player: Player, events: Events) {
            events.containsAny(
                EVENT_PLAYBACK_STATE_CHANGED,
                EVENT_PLAY_WHEN_READY_CHANGED,
                EVENT_IS_PLAYING_CHANGED
            ).then {
                updatePositionLoop()
            }
        }
    }
}