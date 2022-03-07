package com.lalilu.lmusic.event

import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import com.lalilu.lmusic.manager.SearchManager
import com.lalilu.lmusic.utils.moveHeadToTailWithSearch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

@Singleton
class GlobalViewModel @Inject constructor(
    private val searchManager: SearchManager
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
    var searchFor = searchManager::searchFor

    private fun stopUpdate() {
        handler.removeCallbacks(this::updatePosition)
    }

    private var lastPlayState = false
    fun updatePosition(force: Boolean = false) {
        if (force) {
            val position = getPositionFromPlayer()
            launch { currentPosition.emit(position) }
            return
        }
        val isPlaying = getIsPlayingFromPlayer()
        if (lastPlayState == isPlaying) {
            val position = getPositionFromPlayer()
            launch { currentPosition.emit(position) }
        } else {
            lastPlayState = isPlaying
        }
        handler.postDelayed(this::updatePosition, 100)
    }

    val currentPosition: MutableStateFlow<Long> = MutableStateFlow(0L)
    val currentMediaItem: MutableStateFlow<MediaItem?> = MutableStateFlow(null)
    val currentPlaylist: MutableStateFlow<List<MediaItem>> = MutableStateFlow(emptyList())

    val currentPlaylistLiveData = currentPlaylist.combine(currentMediaItem) { items, item ->
        item ?: return@combine items
        items.moveHeadToTailWithSearch(item.mediaId) { listItem, id ->
            listItem.mediaId == id
        }
    }.flowOn(Dispatchers.IO).combine(searchManager.keyword) { items, keyword ->
        if (keyword == null || TextUtils.isEmpty(keyword)) return@combine items
        val keywords = keyword.split(" ")

        items.filter {
            val originStr = "${it.mediaMetadata.title} ${it.mediaMetadata.artist}"
            var resultStr = originStr
            val isContainChinese = searchManager.isContainChinese(originStr)
            val isContainKatakanaOrHinagana =
                searchManager.isContainKatakanaOrHinagana(originStr)
            if (isContainChinese || isContainKatakanaOrHinagana) {
                if (isContainChinese) {
                    val chinese = searchManager.toHanYuPinyinString(originStr)
                    resultStr = "$resultStr $chinese"
                }

                val japanese = searchManager.toHiraString(originStr)
                val romaji = searchManager.toRomajiString(japanese)
                resultStr = "$resultStr $romaji"
            }
            searchManager.checkKeywords(resultStr, keywords)
        }
    }.flowOn(Dispatchers.IO).asLiveData()
    val currentMediaItemLiveData: LiveData<MediaItem?> = currentMediaItem.asLiveData()
    val currentPositionLiveData: LiveData<Long> = currentPosition.asLiveData()

    val playerListener = object : Player.Listener {
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            launch { currentMediaItem.emit(mediaItem) }
            updatePosition(true)
        }
    }
}