package com.lalilu.lmusic.service

import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.text.TextUtils
import androidx.lifecycle.*
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaBrowser
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.SessionToken
import com.blankj.utilcode.util.GsonUtils
import com.google.common.reflect.TypeToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.lalilu.lmusic.Config
import com.lalilu.lmusic.datasource.BaseMediaSource
import com.lalilu.lmusic.datasource.ITEM_PREFIX
import com.lalilu.lmusic.manager.SearchManager
import com.lalilu.lmusic.utils.sources.LyricSourceFactory
import com.lalilu.lmusic.utils.moveHeadToTailWithSearch
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

interface EnhanceBrowser {
    fun playById(mediaId: String): Boolean
    fun addToNext(mediaId: String): Boolean
}

@Singleton
@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class MSongBrowser @Inject constructor(
    @ApplicationContext
    private val mContext: Context,
    private val mediaSource: BaseMediaSource,
    private val searchManager: SearchManager,
    private val lyricSourceFactory: LyricSourceFactory
) : DefaultLifecycleObserver, CoroutineScope, EnhanceBrowser {
    override val coroutineContext: CoroutineContext = Dispatchers.IO
    private lateinit var browserFuture: ListenableFuture<MediaBrowser>
    val browser: MediaBrowser?
        get() = if (browserFuture.isDone) browserFuture.get() else null

    private val playerSp: SharedPreferences by lazy {
        mContext.getSharedPreferences(Config.LAST_PLAYED_SP, Context.MODE_PRIVATE)
    }

    val searchFor = searchManager::searchFor

    private val _currentPositionLiveData: MutableLiveData<Long> = MutableLiveData()
    private val _currentMediaItemFlow: MutableStateFlow<MediaItem?> = MutableStateFlow(null)
    private val _playlistFlow: MutableStateFlow<List<MediaItem>> = MutableStateFlow(emptyList())

    val currentLyricLiveData: LiveData<Pair<String, String?>?> = _currentMediaItemFlow.mapLatest {
        it ?: return@mapLatest null
        return@mapLatest lyricSourceFactory.getLyric(it)
    }.flowOn(Dispatchers.IO)
        .asLiveData()

    var originPlaylistIds: List<String> = emptyList()

    val playlistLiveData: LiveData<List<MediaItem>> =
        _playlistFlow.combine(_currentMediaItemFlow) { items, item ->
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

    val currentMediaItemLiveData: LiveData<MediaItem?> = _currentMediaItemFlow.asLiveData()
    val currentPositionLiveData: LiveData<Long> = _currentPositionLiveData

    @UnstableApi
    override fun onStart(owner: LifecycleOwner) {
        browserFuture = MediaBrowser.Builder(
            mContext, SessionToken(mContext, ComponentName(mContext, MSongService::class.java))
        ).setListener(MyBrowserListener())
            .buildAsync()
        browserFuture.addListener({ onConnected() }, MoreExecutors.directExecutor())
    }

    override fun onStop(owner: LifecycleOwner) {
        MediaBrowser.releaseFuture(browserFuture)
    }

    private inner class MyBrowserListener : MediaBrowser.Listener {
        override fun onChildrenChanged(
            browser: MediaBrowser,
            parentId: String,
            itemCount: Int,
            params: MediaLibraryService.LibraryParams?
        ) {

        }
    }

    private fun onConnected() {
        println("[MSongBrowser]#onConnected")
        val browser = browserFuture.get() ?: return

        if (browser.currentMediaItem == null) {
            recoverLastPlayedItem(browser, recoverLastPlayedList(browser))
        }

        updateCurrentMediaItem(browser.currentMediaItem)
        updateShuffleSwitchUI(browser.shuffleModeEnabled)
        updateRepeatSwitchUI(browser.repeatMode)
        updatePosition()

        browser.addListener(object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                updateCurrentMediaItem(mediaItem)
                updatePosition(true)
            }

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                updateShuffleSwitchUI(shuffleModeEnabled)
            }

            override fun onRepeatModeChanged(repeatMode: Int) {
                updateRepeatSwitchUI(repeatMode)
            }

            override fun onTimelineChanged(timeline: Timeline, reason: Int) {
                val temps = MutableList(browser.mediaItemCount) {
                    return@MutableList mediaSource.getItemById(
                        ITEM_PREFIX + browser.getMediaItemAt(it).mediaId
                    )
                }
                launch(Dispatchers.IO) {
                    originPlaylistIds = temps.mapNotNull { it?.mediaId }
                    _playlistFlow.emit(temps.mapNotNull { it })
                }
            }
        })
    }

    private fun recoverLastPlayedItem(browser: MediaBrowser, ids: List<String>) {
        playerSp.getString(Config.LAST_PLAYED_ID, null)?.let { id ->
            val index = ids.indexOfFirst { it == id }
            if (index >= 0) {
                browser.seekToDefaultPosition(index)
                browser.prepare()
            }
        }
    }

    private fun recoverLastPlayedList(browser: MediaBrowser): List<String> {
        playerSp.getString(Config.LAST_PLAYED_LIST, null)?.let { json ->
            val typeToken = object : TypeToken<List<String>>() {}
            val list = GsonUtils.fromJson<List<String>>(json, typeToken.type)

            browser.setMediaItems(list.mapNotNull {
                mediaSource.getItemById(ITEM_PREFIX + it)
            })
            return list
        }
        return emptyList()
    }

    private fun updateCurrentMediaItem(mediaItem: MediaItem?) =
        launch(Dispatchers.IO) {
            _currentMediaItemFlow.emit(mediaItem)
        }

    private fun updateShuffleSwitchUI(shuffleModeEnabled: Boolean) {
    }

    private fun updateRepeatSwitchUI(repeatMode: Int) {
    }

    private var lastPlayState = false
    private fun updatePosition(force: Boolean = false) {
        browser?.let {
            if (lastPlayState != it.isPlaying && !force) {
                lastPlayState = it.isPlaying
            } else {
                _currentPositionLiveData.postValue(it.currentPosition)
            }
        }
        if (!force) {
            Handler().postDelayed(this::updatePosition, 100)
        }
    }

    override fun playById(mediaId: String): Boolean {
        val index = originPlaylistIds.indexOf(mediaId)
        browser?.seekToDefaultPosition(index)
        return index >= 0
    }

    override fun addToNext(mediaId: String): Boolean {
        val currentIndex = browser?.currentMediaItemIndex ?: return false
        if (currentIndex < 0) return false

        if (originPlaylistIds.contains(mediaId)) {
            val nowIndex = originPlaylistIds.indexOf(mediaId)
            if (nowIndex < 0 || currentIndex == nowIndex) return false

            val targetIndex = if (nowIndex < currentIndex) currentIndex else currentIndex + 1
            browser?.moveMediaItem(nowIndex, targetIndex)
        } else {
            val item = mediaSource.getItemById(ITEM_PREFIX + mediaId) ?: return false
            browser?.addMediaItem(currentIndex + 1, item)
        }
        return true
    }
}