package com.lalilu.lplayer.service

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaLibraryService.MediaLibrarySession
import androidx.media3.session.MediaSession
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext

class MService : MediaLibraryService(),
    MediaLibrarySession.Callback,
    CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.IO + SupervisorJob()

    private var exoPlayer: ExoPlayer? = null
    private var mediaSession: MediaLibrarySession? = null

    override fun onCreate() {
        super.onCreate()

        exoPlayer = ExoPlayer
            .Builder(this)
            .build()

        mediaSession = MediaLibrarySession
            .Builder(this, exoPlayer!!, this)
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
    ): MediaLibrarySession? {
        return mediaSession
    }

    @OptIn(UnstableApi::class)
    override fun onPlaybackResumption(
        mediaSession: MediaSession,
        controller: MediaSession.ControllerInfo
    ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> {
        // TODO 待完成继续播放的逻辑
        return super.onPlaybackResumption(mediaSession, controller)
    }
}