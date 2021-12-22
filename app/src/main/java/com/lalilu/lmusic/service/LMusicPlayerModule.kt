package com.lalilu.lmusic.service

import android.content.ComponentName
import android.content.Context
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.lalilu.lmusic.Config.LAST_METADATA
import com.lalilu.lmusic.Config.LAST_PLAYBACK_STATE
import com.lalilu.lmusic.utils.Mathf
import com.tencent.mmkv.MMKV
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.logging.Logger
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

@Singleton
class LMusicPlayerModule @Inject constructor(
    @ApplicationContext context: Context
) : ViewModel(), CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Default

    private val logger = Logger.getLogger(this.javaClass.name)
    private val mmkv = MMKV.defaultMMKV()

    private val _metadata =
        MutableStateFlow(mmkv.decodeParcelable(LAST_METADATA, MediaMetadataCompat::class.java))

    private val _playBackState =
        MutableStateFlow(
            mmkv.decodeParcelable(LAST_PLAYBACK_STATE, PlaybackStateCompat::class.java)
        )

    private val _mediaItems: MutableStateFlow<MutableList<MediaBrowserCompat.MediaItem>> =
        MutableStateFlow(ArrayList())

    @ExperimentalCoroutinesApi
    val metadata: LiveData<MediaMetadataCompat?>
        get() = _metadata.mapLatest {
            if (it != null) mmkv.encode(LAST_METADATA, it)
            return@mapLatest it
        }.asLiveData()

    @ExperimentalCoroutinesApi
    val playBackState: LiveData<PlaybackStateCompat?>
        get() = _playBackState.mapLatest {
            if (it != null) mmkv.encode(LAST_PLAYBACK_STATE, it)
            return@mapLatest it
        }.asLiveData()

    val mediaItem: LiveData<MutableList<MediaBrowserCompat.MediaItem>>
        get() = _mediaItems.combine(_metadata) { mediaItem, metadata ->
            listOrderChange(mediaItem, metadata?.description?.mediaId) ?: mediaItem
        }.flowOn(Dispatchers.Default)
            .conflate()
            .asLiveData()

    lateinit var mediaController: MediaControllerCompat
    private var controllerCallback: MusicControllerCallback = MusicControllerCallback()
    private var connectionCallback: MusicConnectionCallback = MusicConnectionCallback(context)
    private var subscriptionCallback: MusicSubscriptionCallback = MusicSubscriptionCallback()
    private var mediaBrowser: MediaBrowserCompat = MediaBrowserCompat(
        context, ComponentName(context, MSongService::class.java),
        connectionCallback, null
    )

    private fun listOrderChange(
        oldList: List<MediaBrowserCompat.MediaItem>,
        mediaId: String?
    ): MutableList<MediaBrowserCompat.MediaItem>? {
        mediaId ?: return null

        val nowPosition = oldList.indexOfFirst { item ->
            item.description.mediaId == mediaId
        }
        if (nowPosition == -1) return null

        return ArrayList(oldList.map { song ->
            val position = Mathf.clampInLoop(
                0, oldList.size - 1, oldList.indexOf(song), nowPosition
            )
            oldList[position]
        })
    }

    fun connect() = mediaBrowser.connect()
    fun disconnect() = mediaBrowser.disconnect().also {
        mediaController.unregisterCallback(controllerCallback)
    }

    fun subscribe(parentId: String) = mediaBrowser.subscribe(parentId, subscriptionCallback)

    inner class MusicConnectionCallback(private val context: Context) :
        MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            mediaController = MediaControllerCompat(context, mediaBrowser.sessionToken).apply {
                registerCallback(controllerCallback)
            }

            subscribe(MSongService.MEDIA_ID_EMPTY_ROOT)
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
                logger.info("[MusicControllerCallback]#onPlaybackStateChanged: ${state.state}")
            }
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            launch {
                _metadata.emit(metadata ?: return@launch)
                logger.info("[MusicControllerCallback]#onMetadataChanged: ${metadata.description.title}")
            }
        }
    }
}