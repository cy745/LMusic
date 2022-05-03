package com.lalilu.lmusic.service

import android.app.PendingIntent
import androidx.media3.common.*
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaController
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.SPUtils
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.lalilu.lmusic.Config
import com.lalilu.lmusic.datasource.BaseMediaSource
import com.lalilu.lmusic.datasource.ITEM_PREFIX
import com.lalilu.lmusic.manager.SpManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
    lateinit var mediaSource: BaseMediaSource

    @Inject
    lateinit var notificationProvider: LMusicNotificationProvider

    private val lastPlayedSp: SPUtils by lazy {
        SPUtils.getInstance(Config.LAST_PLAYED_SP)
    }

    private val audioAttributes = AudioAttributes.Builder()
        .setContentType(C.CONTENT_TYPE_MUSIC)
        .setUsage(C.USAGE_MEDIA)
        .build()

    override fun onCreate() {
        super.onCreate()
        exoPlayer = ExoPlayer.Builder(this)
            .setUseLazyPreparation(false)
            .setHandleAudioBecomingNoisy(true)
            .setUseLazyPreparation(false)
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

        SpManager.listen("KEY_SETTINGS_ignore_audio_focus",
            SpManager.SpBoolListener(false) {
                exoPlayer.setAudioAttributes(audioAttributes, !it)
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
                .setMediaItemFiller(CustomMediaItemFiller())
                .setSessionActivity(pendingIntent)
                .build()

        val controllerFuture =
            MediaController.Builder(this, mediaLibrarySession.token)
                .buildAsync()

        controllerFuture.addListener({
            mediaController = controllerFuture.get()
            mediaController.addListener(GlobalData.playerListener)
            mediaController.addListener(LastPlayedListener())
            GlobalData.getIsPlayingFromPlayer = mediaController::isPlaying
            GlobalData.getPositionFromPlayer = mediaController::getCurrentPosition
        }, MoreExecutors.directExecutor())

        setMediaNotificationProvider(notificationProvider)
    }

    private inner class LastPlayedListener : Player.Listener {
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            launch {
                lastPlayedSp.put(Config.LAST_PLAYED_ID, mediaItem?.mediaId)
            }
        }

        override fun onTimelineChanged(timeline: Timeline, reason: Int) {
            val list = List(mediaController.mediaItemCount) {
                mediaController.getMediaItemAt(it).mediaId
            }
            launch {
                lastPlayedSp.put(Config.LAST_PLAYED_LIST, GsonUtils.toJson(list))
            }
        }
    }

    private inner class CustomMediaItemFiller : MediaSession.MediaItemFiller {
        override fun fillInLocalConfiguration(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItem: MediaItem
        ): MediaItem {
            return mediaSource.getItemById(ITEM_PREFIX + mediaItem.mediaId) ?: mediaItem
        }
    }

    private inner class CustomMediaLibrarySessionCallback :
        MediaLibrarySession.MediaLibrarySessionCallback {
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