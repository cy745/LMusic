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
import com.lalilu.lmusic.domain.entity.MSong
import com.lalilu.lmusic.manager.SearchManager
import com.lalilu.lmusic.service.MSongService
import com.lalilu.lmusic.utils.getLastMediaMetadata
import com.lalilu.lmusic.utils.getLastPlaybackState
import com.lalilu.lmusic.utils.moveHeadToTailWithSearch
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.logging.Logger
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext


@Singleton
@ExperimentalCoroutinesApi
class PlayerModule @Inject constructor(
    @ApplicationContext context: Context,
    dataModule: DataModule,
    private val searchManager: SearchManager
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

    private val _mSongs: Flow<List<MSong>> =
        dataModule.nowListFlow.combine(_metadata) { items, metadata ->
            items.moveHeadToTailWithSearch(metadata.description.mediaId) { item, id ->
                item.songId.toString() == id
            }
        }.combine(searchManager.keyword) { items, keyword ->
            if (keyword == null || TextUtils.isEmpty(keyword)) return@combine items
            val keywords = keyword.split(" ")

            items.filter {
                val originStr = "${it.songTitle} ${it.showingArtist}"
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
        }

    val mSongsLiveData: LiveData<List<MSong>> = _mSongs.asLiveData()
    val metadataLiveData: LiveData<MediaMetadataCompat?> = _metadata.asLiveData()

    fun searchFor(keyword: String?) = searchManager.searchFor(keyword)

    fun connect() {
        mediaBrowser.connect()
    }

    fun disconnect() {
        mediaController?.unregisterCallback(controllerCallback)
        mediaBrowser.disconnect()
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
            logger.info("[MusicSubscriptionCallback]#onChildrenLoaded: $parentId")
        }
    }

    inner class MusicControllerCallback : MediaControllerCompat.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            launch {
                _playBackState.emit(state ?: return@launch)
                logger.info("[MusicControllerCallback]#onPlaybackStateChanged: ${state.state} ${state.position}")
            }
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            launch {
                _metadata.emit(metadata ?: return@launch)
                logger.info("[MusicControllerCallback]#onMetadataChanged: ${metadata.description?.title}")
            }
        }
    }
}