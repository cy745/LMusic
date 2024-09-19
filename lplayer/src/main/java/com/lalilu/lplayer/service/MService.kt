package com.lalilu.lplayer.service

import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaLibraryService.LibraryParams
import androidx.media3.session.MediaLibraryService.MediaLibrarySession
import androidx.media3.session.MediaSession
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.lalilu.lmedia2.LMedia
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext

class MService : MediaLibraryService(), CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.IO + SupervisorJob()

    private var exoPlayer: ExoPlayer? = null
    private var mediaSession: MediaLibrarySession? = null

    override fun onCreate() {
        super.onCreate()

        exoPlayer = ExoPlayer
            .Builder(this)
            .build()

        mediaSession = MediaLibrarySession
            .Builder(this, exoPlayer!!, MServiceCallback())
            .build()
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
}

@UnstableApi
class MServiceCallback : MediaLibrarySession.Callback {
    companion object {
        const val ROOT = "root"
        const val ALL_SONGS = "all_songs"
        const val ALL_ARTISTS = "all_artists"
        const val ALL_ALBUMS = "all_albums"
    }

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
        LibraryResult.ofItem(buildBrowsableItem(ROOT, "LMedia Library"), params)
    )

    override fun onGetChildren(
        session: MediaLibrarySession,
        browser: MediaSession.ControllerInfo,
        parentId: String,
        page: Int,
        pageSize: Int,
        params: LibraryParams?
    ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
        if (parentId == ROOT) {
            return Futures.immediateFuture(
                LibraryResult.ofItemList(
                    listOf(
                        buildBrowsableItem(ALL_SONGS, "All Songs"),
                        buildBrowsableItem(ALL_ARTISTS, "All Artists"),
                        buildBrowsableItem(ALL_ALBUMS, "All Albums")
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
            else LibraryResult.ofError(LibraryResult.RESULT_ERROR_BAD_VALUE)
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

    @OptIn(UnstableApi::class)
    override fun onPlaybackResumption(
        mediaSession: MediaSession,
        controller: MediaSession.ControllerInfo
    ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> {
        // TODO 待完成继续播放的逻辑
        return Futures.immediateFuture(
            MediaSession.MediaItemsWithStartPosition(emptyList(), 0, 0L)
        )
    }
}