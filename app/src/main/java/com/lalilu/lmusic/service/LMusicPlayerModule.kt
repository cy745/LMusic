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
import com.lalilu.lmusic.Config
import com.lalilu.lmusic.Config.LAST_METADATA
import com.lalilu.lmusic.Config.LAST_PLAYBACK_STATE
import com.lalilu.lmusic.Config.LAST_POSITION
import com.lalilu.lmusic.database.LMusicDataBase
import com.lalilu.lmusic.domain.entity.MSongDetail
import com.lalilu.lmusic.utils.Mathf
import com.tencent.mmkv.MMKV
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
    database: LMusicDataBase
) : ViewModel(), CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.IO

    private val logger = Logger.getLogger(this.javaClass.name)
    private val mmkv = MMKV.defaultMMKV()

    private val _metadata = MutableStateFlow(
        mmkv.decodeParcelable(LAST_METADATA, MediaMetadataCompat::class.java)
    )

    private val _playBackState = MutableStateFlow(
        mmkv.decodeParcelable(LAST_PLAYBACK_STATE, PlaybackStateCompat::class.java).let {
            PlaybackStateCompat.Builder().setState(
                PlaybackStateCompat.STATE_STOPPED,
                it?.position ?: 0L,
                it?.playbackSpeed ?: 1.0f
            ).build()
        }
    )

    private val _mediaItems: MutableStateFlow<MutableList<MediaBrowserCompat.MediaItem>> =
        MutableStateFlow(ArrayList())

    private val _songPosition: Flow<Long> = _playBackState.flatMapLatest {
        var currentDuration = Mathf.getPositionFromPlaybackStateCompat(it)
        if (it.state == PlaybackStateCompat.STATE_STOPPED)
            currentDuration = mmkv.decodeLong(LAST_POSITION)

        flow {
            if (it.state == PlaybackStateCompat.STATE_PLAYING) {
                repeat(Int.MAX_VALUE) {
                    emit(currentDuration)
                    currentDuration += 1000
                    delay(1000)
                }
            } else emit(currentDuration)
        }
    }.map {
        mmkv.encode(LAST_POSITION, it)
        return@map it
    }

    val metadata: LiveData<MediaMetadataCompat?> = _metadata.asLiveData()

    val mediaItems: LiveData<MutableList<MediaBrowserCompat.MediaItem>> =
        _mediaItems.combine(_metadata) { mediaItem, metadata ->
            listOrderChange(mediaItem, metadata?.description?.mediaId) ?: mediaItem
        }.asLiveData()

    val songDetail: LiveData<MSongDetail?> = _metadata.flatMapLatest {
        database.songDetailDao().getByIdStrFlow(it?.description?.mediaId ?: "-1")
    }.asLiveData()

    val songPosition: LiveData<Long> = _songPosition.asLiveData()

    var mediaController: MediaControllerCompat? = null
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
        mediaController?.unregisterCallback(controllerCallback)
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
                mmkv.encode(LAST_PLAYBACK_STATE, state)
                logger.info("[MusicControllerCallback]#onPlaybackStateChanged: ${state.state} ${state.position}")
            }
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            launch {
                _metadata.emit(metadata ?: return@launch)
                mmkv.encode(LAST_METADATA, metadata)
                logger.info("[MusicControllerCallback]#onMetadataChanged: ${metadata.description?.title}")
            }
        }
    }
}