package com.lalilu.lmusic.service

import android.app.PendingIntent
import androidx.media3.common.*
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaController
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.lalilu.lmusic.Config
import com.lalilu.lmusic.datasource.MMediaSource
import com.lalilu.lmusic.manager.GlobalDataManager
import com.lalilu.lmusic.manager.HistoryManager
import com.lalilu.lmusic.manager.SpManager
import com.lalilu.lmusic.utils.RepeatMode
import com.lalilu.lmusic.utils.safeLaunch
import com.lalilu.lmusic.utils.then
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

@UnstableApi
@AndroidEntryPoint
class MSongService : MediaLibraryService(), CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.IO
    private lateinit var player: Player
    private lateinit var exoPlayer: ExoPlayer

    private lateinit var mediaLibrarySession: MediaLibrarySession
    private lateinit var mediaController: MediaController

    @Inject
    lateinit var mediaSource: MMediaSource

    @Inject
    lateinit var globalDataManager: GlobalDataManager

    @Inject
    lateinit var notificationProvider: LMusicNotificationProvider

    private val audioAttributes = AudioAttributes.Builder()
        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
        .setUsage(C.USAGE_MEDIA)
        .build()

    override fun onCreate() {
        super.onCreate()
        exoPlayer = ExoPlayer.Builder(this)
            .setUseLazyPreparation(false)
            .setHandleAudioBecomingNoisy(true)
            .build()
        player = object : ForwardingPlayer(exoPlayer) {
            override fun getMaxSeekToPreviousPosition(): Long = Long.MAX_VALUE
            override fun seekToPrevious() {
                if (player.hasPreviousMediaItem() && player.currentPosition <= maxSeekToPreviousPosition) {
                    seekToPreviousMediaItem()
                    return
                }
                super.seekToPrevious()
            }
        }

        SpManager.listen(Config.KEY_SETTINGS_IGNORE_AUDIO_FOCUS,
            SpManager.SpBoolListener(Config.DEFAULT_SETTINGS_IGNORE_AUDIO_FOCUS) {
                exoPlayer.setAudioAttributes(audioAttributes, !it)
            })

        SpManager.listen(Config.KEY_SETTINGS_REPEAT_MODE,
            SpManager.SpIntListener(Config.DEFAULT_SETTINGS_REPEAT_MODE) {
                RepeatMode.values().getOrNull(it)?.let { repeatMode ->
                    exoPlayer.shuffleModeEnabled = repeatMode.isShuffle
                    exoPlayer.repeatMode = repeatMode.repeatMode
                }
            })

        val pendingIntent: PendingIntent =
            packageManager.getLaunchIntentForPackage(packageName).let { sessionIntent ->
                PendingIntent.getActivity(
                    this, 0, sessionIntent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
            }

        mediaLibrarySession =
            MediaLibrarySession.Builder(this, player, CustomMediaLibrarySessionCallback())
                .setSessionActivity(pendingIntent)
                .build()

        val controllerFuture =
            MediaController.Builder(this, mediaLibrarySession.token)
                .buildAsync()

        controllerFuture.addListener({
            mediaController = controllerFuture.get()
            mediaController.addListener(globalDataManager.playerListener)
            mediaController.addListener(LastPlayedListener())
            globalDataManager.player = mediaController
        }, MoreExecutors.directExecutor())

        setMediaNotificationProvider(notificationProvider)
    }

    private inner class LastPlayedListener : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            events.containsAny(
                Player.EVENT_SHUFFLE_MODE_ENABLED_CHANGED,
                Player.EVENT_REPEAT_MODE_CHANGED,
                Player.EVENT_MEDIA_ITEM_TRANSITION
            ).then {
                onUpdateNotification(mediaLibrarySession)
            }
        }

        override fun onTimelineChanged(timeline: Timeline, reason: Int) {
            HistoryManager.currentPlayingIds = List(mediaController.mediaItemCount) {
                mediaController.getMediaItemAt(it).mediaId
            }

            safeLaunch {
                globalDataManager.currentPlaylist.emit(
                    HistoryManager.currentPlayingIds.let {
                        mediaSource.getItemsByIds(it)
                    }
                )
            }
        }
    }

    private inner class CustomMediaLibrarySessionCallback : MediaLibrarySession.Callback {
        override fun onAddMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: MutableList<MediaItem>
        ): ListenableFuture<MutableList<MediaItem>> {
            val mediaIds = mediaItems.map { it.mediaId }
            val items = mediaSource.getItemsByIds(mediaIds)
            return Futures.immediateFuture(items.toMutableList())
        }

        override fun onGetLibraryRoot(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<MediaItem>> {
            return Futures.immediateFuture(LibraryResult.ofItem(mediaSource.getRootItem(), params))
        }

        override fun onGetItem(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            mediaId: String
        ): ListenableFuture<LibraryResult<MediaItem>> {
            val item = mediaSource.getItemById(mediaId) ?: return Futures.immediateFuture(
                LibraryResult.ofError(LibraryResult.RESULT_ERROR_BAD_VALUE)
            )
            return Futures.immediateFuture(LibraryResult.ofItem(item, null))
        }

        override fun onGetChildren(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            parentId: String,
            page: Int,
            pageSize: Int,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
            val children = mediaSource.getChildren(parentId) ?: return Futures.immediateFuture(
                LibraryResult.ofError(LibraryResult.RESULT_ERROR_BAD_VALUE)
            )
            return Futures.immediateFuture(LibraryResult.ofItemList(children, params))
        }
    }

    override fun onDestroy() {
        player.release()
        mediaLibrarySession.release()
        super.onDestroy()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession {
        return mediaLibrarySession
    }
}