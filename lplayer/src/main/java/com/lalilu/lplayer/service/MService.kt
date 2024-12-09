package com.lalilu.lplayer.service

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaLibraryService.LibraryParams
import androidx.media3.session.MediaLibraryService.MediaLibrarySession
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionError
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.AppUtils
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.lalilu.lmedia.LMedia
import com.lalilu.lplayer.MPlayerKV
import com.lalilu.lplayer.extensions.FadeTransitionRenderersFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

@OptIn(UnstableApi::class)
class MService : MediaLibraryService(), CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.IO + SupervisorJob()

    private var exoPlayer: ExoPlayer? = null
    private var mediaSession: MediaLibrarySession? = null
    private val defaultAudioAttributes by lazy {
        AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setSpatializationBehavior(C.SPATIALIZATION_BEHAVIOR_AUTO)
            .setAllowedCapturePolicy(C.ALLOW_CAPTURE_BY_ALL)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()
    }

    override fun onCreate() {
        super.onCreate()

        setMediaNotificationProvider(
            MNotificationProvider(this)
        )

        exoPlayer = ExoPlayer
            .Builder(this)
            .setRenderersFactory(FadeTransitionRenderersFactory(this, this))
            .setHandleAudioBecomingNoisy(MPlayerKV.handleBecomeNoisy.value ?: true)
            .setAudioAttributes(defaultAudioAttributes, MPlayerKV.handleAudioFocus.value ?: true)
            .build()

        mediaSession = MediaLibrarySession
            .Builder(this, exoPlayer!!, MServiceCallback())
            .setSessionActivity(getLauncherPendingIntent())
            .build()

        startListenForValuesUpdate()
    }

    override fun onDestroy() {
        // 释放相关实例
        exoPlayer?.stop()
        exoPlayer?.release()
        exoPlayer = null
        mediaSession?.release()
        mediaSession = null
        super.onDestroy()
    }

    override fun onGetSession(
        controllerInfo: MediaSession.ControllerInfo
    ): MediaLibrarySession? = mediaSession

    private fun startListenForValuesUpdate() = launch {
        MPlayerKV.handleAudioFocus.flow().onEach {
            withContext(Dispatchers.Main) {
                exoPlayer?.setAudioAttributes(defaultAudioAttributes, it ?: true)
            }
        }.launchIn(this)

        MPlayerKV.handleBecomeNoisy.flow().onEach {
            withContext(Dispatchers.Main) {
                exoPlayer?.setHandleAudioBecomingNoisy(it ?: true)
            }
        }.launchIn(this)
    }
}

@OptIn(UnstableApi::class)
private class MServiceCallback : MediaLibrarySession.Callback {
    private fun buildBrowsableItem(id: String, title: String): MediaItem {
        val metadata = MediaMetadata.Builder()
            .setTitle(title)
            .setIsBrowsable(true)
            .setIsPlayable(false)
            .build()

        return MediaItem.Builder()
            .setMediaId(id)
            .setMediaMetadata(metadata)
            .build()
    }

    private fun resolveMediaItems(mediaItems: List<MediaItem>): List<MediaItem> {
        return mediaItems.mapNotNull { item -> LMedia.getItem(item.mediaId) }
    }

    override fun onGetLibraryRoot(
        session: MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        params: LibraryParams?
    ): ListenableFuture<LibraryResult<MediaItem>> = Futures.immediateFuture(
        LibraryResult.ofItem(buildBrowsableItem(LMedia.ROOT, "LMedia Library"), params)
    )

    override fun onGetChildren(
        session: MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        parentId: String,
        page: Int,
        pageSize: Int,
        params: LibraryParams?
    ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
        if (parentId == LMedia.ROOT) {
            return Futures.immediateFuture(
                LibraryResult.ofItemList(
                    listOf(
                        buildBrowsableItem(LMedia.ALL_SONGS, "All Songs"),
                        buildBrowsableItem(LMedia.ALL_ARTISTS, "All Artists"),
                        buildBrowsableItem(LMedia.ALL_ALBUMS, "All Albums")
                    ),
                    params
                )
            )
        }

        return Futures.immediateFuture(
            LibraryResult.ofItemList(LMedia.getChildren(parentId), params)
        )
    }

    override fun onGetItem(
        session: MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        mediaId: String
    ): ListenableFuture<LibraryResult<MediaItem>> {
        val item = LMedia.getItem(mediaId)

        return Futures.immediateFuture(
            if (item != null) LibraryResult.ofItem(item, null)
            else LibraryResult.ofError(SessionError.ERROR_BAD_VALUE)
        )
    }

    override fun onSetMediaItems(
        mediaSession: MediaSession,
        controller: MediaSession.ControllerInfo,
        mediaItems: MutableList<MediaItem>,
        startIndex: Int,
        startPositionMs: Long
    ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> = Futures.immediateFuture(
        MediaSession.MediaItemsWithStartPosition(
            resolveMediaItems(mediaItems),
            startIndex,
            startPositionMs
        )
    )

    override fun onAddMediaItems(
        mediaSession: MediaSession,
        controller: MediaSession.ControllerInfo,
        mediaItems: MutableList<MediaItem>
    ): ListenableFuture<MutableList<MediaItem>> = Futures.immediateFuture(
        resolveMediaItems(mediaItems).toMutableList()
    )

    override fun onPlaybackResumption(
        mediaSession: MediaSession,
        controller: MediaSession.ControllerInfo
    ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> {
        return Futures.submitAsync({
            Futures.immediateFuture(
                MediaSession.MediaItemsWithStartPosition(getHistoryItems(), 0, 0L)
            )
        }, Dispatchers.IO.asExecutor())
    }
}

private fun Context.getLauncherPendingIntent(): PendingIntent {
    return PendingIntent.getActivity(
        this,
        0,
        Intent().apply {
            setClassName(
                AppUtils.getAppPackageName(),
                ActivityUtils.getLauncherActivity()
            )
        },
        PendingIntent.FLAG_IMMUTABLE
    )
}

internal fun getHistoryItems(): List<MediaItem> {
    val history = MPlayerKV.historyPlaylistIds.get()

    return if (!history.isNullOrEmpty()) LMedia.mapItems(history)
    else LMedia.getChildren(LMedia.ALL_SONGS)
}

internal fun saveHistoryIds(mediaIds: List<String>) {
    MPlayerKV.historyPlaylistIds.set(mediaIds)
}