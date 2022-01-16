package com.lalilu.lmusic.event

import android.content.ComponentName
import android.content.Context
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.text.TextUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.lalilu.lmusic.Config
import com.lalilu.lmusic.database.LMusicDataBase
import com.lalilu.lmusic.domain.entity.FullSongInfo
import com.lalilu.lmusic.manager.SearchManager
import com.lalilu.lmusic.service.MSongService
import com.lalilu.lmusic.utils.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.*
import java.util.logging.Logger
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.ArrayList
import kotlin.coroutines.CoroutineContext


@Singleton
@ExperimentalCoroutinesApi
class LMusicPlayerModule @Inject constructor(
    @ApplicationContext context: Context,
    private val searchManager: SearchManager,
    private val database: LMusicDataBase,
    private val dataModule: DataModule
) : ViewModel(), CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.IO

    private val logger = Logger.getLogger(this.javaClass.name)
    private val sharedPref = context.getSharedPreferences(
        Config.SHARED_PLAYER, Context.MODE_PRIVATE
    )

    var mediaController: MediaControllerCompat? = null
    private var controllerCallback: MusicControllerCallback = MusicControllerCallback()
    private var connectionCallback: MusicConnectionCallback = MusicConnectionCallback(context)
    private var subscriptionCallback: MusicSubscriptionCallback = MusicSubscriptionCallback()
    private var mediaBrowser: MediaBrowserCompat = MediaBrowserCompat(
        context, ComponentName(context, MSongService::class.java),
        connectionCallback, null
    )

    private val _metadata = MutableStateFlow(sharedPref.getLastMediaMetadata())
    private val _playBackState = MutableStateFlow(sharedPref.getLastPlaybackState())
    private val _mediaItems: MutableStateFlow<MutableList<MediaBrowserCompat.MediaItem>> =
        MutableStateFlow(ArrayList())
    private val mediaItems: Flow<List<MediaBrowserCompat.MediaItem>> =
        _mediaItems.combine(_metadata) { items, metadata ->
            listOrderChange(items, metadata.description?.mediaId) ?: items
        }.combine(searchManager.mKanhira) { items, kanhira ->
            if (kanhira == null) return@combine items

            items.onEach {
                val originStr = "${it.description.title} ${it.description.subtitle}"
                var resultStr = originStr
                val isContainChinese = searchManager.isContainChinese(originStr)
                val isContainKatakanaOrHinagana =
                    searchManager.isContainKatakanaOrHinagana(originStr)
                if (isContainChinese || isContainKatakanaOrHinagana) {
                    if (isContainChinese) {
                        val chinese = searchManager.toHanYuPinyinString(originStr)
                        resultStr = "$resultStr $chinese"
                    }

                    val japanese = searchManager.toHiraString(originStr, kanhira)
                    val romaji = searchManager.toRomajiString(japanese)
                    resultStr = "$resultStr $romaji"
                }
                it.description.extras?.putString("searchStr", resultStr)
            }
        }.combine(searchManager.keyword) { items, keyword ->
            if (keyword == null || TextUtils.isEmpty(keyword)) return@combine items

            val keywords = keyword.split(" ")

            return@combine items.filter { item ->
                val originStr = item.description.extras?.getString("searchStr")
                searchManager.checkKeywords(originStr, keywords)
            }
        }

    //    private val _mSongs: Flow<List<FullSongInfo>> =
//        mediaItems.combine(database.songDao().getAllFullSongFlow()) { items, list ->
//            items.map { item -> list.first { it.song.songId.toString() == item.mediaId } }
//        }
    private val _mSongs = dataModule.nowListFlow.combine(_metadata) { items, metadata ->
        listOrderChanges(items, metadata.description.mediaId) { item, id ->
            item.song.songId.toString() == id
        } ?: items
    }

    val mSongsLiveData: LiveData<List<FullSongInfo>> = _mSongs.asLiveData()
    val metadataLiveData: LiveData<MediaMetadataCompat?> = _metadata.asLiveData()

    fun searchFor(keyword: String?) = searchManager.searchFor(keyword)

    fun connect() {
        if (mediaBrowser.isConnected) mediaBrowser.disconnect()
        if (!mediaBrowser.isConnected) mediaBrowser.connect()
    }

    fun disconnect() {
        if (mediaBrowser.isConnected) {
            mediaBrowser.disconnect().also {
                mediaController?.unregisterCallback(controllerCallback)
            }
        }
    }

    fun subscribe(parentId: String) = mediaBrowser.subscribe(parentId, subscriptionCallback)

    inner class MusicConnectionCallback(private val context: Context) :
        MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            mediaController = MediaControllerCompat(context, mediaBrowser.sessionToken).apply {
                registerCallback(controllerCallback)
            }

            subscribe(Config.MEDIA_ID_EMPTY_ROOT)
            logger.info("[MusicConnectionCallback]#onConnected")
        }
    }

    inner class MusicSubscriptionCallback : MediaBrowserCompat.SubscriptionCallback() {
        override fun onChildrenLoaded(
            parentId: String,
            children: MutableList<MediaBrowserCompat.MediaItem>
        ) {
            launch {
                _mediaItems.emit(children)
                logger.info("[MusicSubscriptionCallback]#onChildrenLoaded: $parentId")
            }
        }
    }

    inner class MusicControllerCallback : MediaControllerCompat.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            launch {
                _playBackState.emit(state ?: return@launch)
                state.saveTo(sharedPref)
                logger.info("[MusicControllerCallback]#onPlaybackStateChanged: ${state.state} ${state.position}")
            }
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            launch {
                _metadata.emit(metadata ?: return@launch)
                metadata.saveTo(sharedPref)
                logger.info("[MusicControllerCallback]#onMetadataChanged: ${metadata.description?.title}")
            }
        }
    }

    private fun <T, K> listOrderChanges(
        list: List<T>,
        Id: K?,
        checkIsSame: (T, K) -> Boolean
    ): MutableList<T>? {
        Id ?: return null

        val nowPosition = list.indexOfFirst { checkIsSame(it, Id) }
        if (nowPosition == -1) return null

        return ArrayList(list.map { item ->
            list[Mathf.clampInLoop(0, list.size - 1, list.indexOf(item), nowPosition)]
        })
    }

    private fun listOrderChange(
        oldList: List<MediaBrowserCompat.MediaItem>,
        mediaId: String?
    ): MutableList<MediaBrowserCompat.MediaItem>? {
        mediaId ?: return null

        val nowPosition = oldList.indexOfFirst { it.description.mediaId == mediaId }
        if (nowPosition == -1) return null

        return ArrayList(oldList.map { song ->
            val position = Mathf.clampInLoop(
                0, oldList.size - 1, oldList.indexOf(song), nowPosition
            )
            oldList[position]
        })
    }
}
