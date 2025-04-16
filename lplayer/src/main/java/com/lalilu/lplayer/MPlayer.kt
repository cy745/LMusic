package com.lalilu.lplayer

import android.content.ComponentName
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaBrowser
import androidx.media3.session.SessionToken
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.Utils
import com.lalilu.lmedia.LMedia
import com.lalilu.lplayer.action.Action
import com.lalilu.lplayer.action.PlayerAction
import com.lalilu.lplayer.extensions.PlayMode
import com.lalilu.lplayer.extensions.playMode
import com.lalilu.lplayer.service.CustomCommand
import com.lalilu.lplayer.service.MService
import com.lalilu.lplayer.service.getHistoryItems
import com.lalilu.lplayer.service.saveHistoryIds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch
import org.koin.dsl.module
import kotlin.coroutines.CoroutineContext

@OptIn(UnstableApi::class)
object MPlayer : CoroutineScope, Player.Listener {
    override val coroutineContext: CoroutineContext = Dispatchers.IO
    private val sessionToken by lazy {
        SessionToken(Utils.getApp(), ComponentName(Utils.getApp(), MService::class.java))
    }

    private var browserInstance: MediaBrowser? = null
    private val browserFuture by lazy {
        MediaBrowser
            .Builder(Utils.getApp(), sessionToken)
            .buildAsync()
    }

    val module = module {
    }

    var pauseWhenCompletion: Boolean by mutableStateOf(false)
    var isPlaying: Boolean by mutableStateOf(false)
        private set
    var currentMediaItem by mutableStateOf<MediaItem?>(null)
        private set
    var currentMediaMetadata: MediaMetadata? by mutableStateOf(null)
        private set
    var currentPlaylistMetadata: MediaMetadata? by mutableStateOf(null)
        private set
    var currentDuration: Long by mutableLongStateOf(0L)
        private set
    var currentTimelineItems by mutableStateOf<List<MediaItem>>(emptyList())
        private set

    val currentPosition: Long
        get() = runCatching { if (browserFuture.isDone) browserFuture.get()?.currentPosition else null }
            .getOrNull() ?: 0L

    val currentBufferedPosition: Long
        get() = runCatching { if (browserFuture.isDone) browserFuture.get()?.bufferedPosition else null }
            .getOrNull() ?: 0L

    fun isItemPlaying(mediaId: String): Boolean {
        if (!isPlaying) return false
        return currentMediaItem?.mediaId == mediaId
    }

    internal fun init() {
        launch(Dispatchers.Main) {
            val browser = browserFuture.await()
            browserInstance = browser
            browser.addListener(this@MPlayer)

            val items = getHistoryItems()
            if (items.isEmpty()) {
                LogUtils.i("No songs found")
                return@launch
            }

            browser.playWhenReady = false
            browser.setMediaItems(items)
            browser.prepare()
        }
    }

    fun doAction(action: Action) = launch(Dispatchers.Main) {
        val browser = browserFuture.await()

        when (action) {
            PlayerAction.Play -> browser.play()
            PlayerAction.Pause -> browser.pause()

            PlayerAction.SkipToNext -> {
                if (browser.playMode == PlayMode.Shuffle) {
                    browser.sendCustomCommand(
                        CustomCommand.SeekToNext.toSessionCommand(),
                        Bundle.EMPTY
                    )
                } else {
                    browser.seekToNext()
                }
            }

            PlayerAction.SkipToPrevious -> {
                if (browser.playMode == PlayMode.Shuffle) {
                    browser.sendCustomCommand(
                        CustomCommand.SeekToPrevious.toSessionCommand(),
                        Bundle.EMPTY
                    )
                } else {
                    browser.seekToPrevious()
                }
            }

            PlayerAction.PlayOrPause -> {
                if (browser.isPlaying) {
                    browser.pause()
                } else {
                    browser.play()
                }
            }

            is PlayerAction.PlayById -> {
                browser.getItem(action.mediaId).await().value?.let {
                    val index = browser.currentTimeline.indexOf(action.mediaId)

                    if (index == -1) {
                        val item = browser.getItem(action.mediaId)
                            .await().value ?: return@launch

                        browser.addMediaItem(0, item)
                        browser.prepare()
                        browser.play()
                    } else {
                        browser.seekTo(index, 0)
                        browser.play()
                    }
                }
            }

            is PlayerAction.SeekTo -> {
                browser.seekTo(action.positionMs)
            }

            is PlayerAction.CustomAction -> {}
            is PlayerAction.PauseWhenCompletion -> {
                pauseWhenCompletion = !action.cancel
            }

            is PlayerAction.SetPlayMode -> {
                browser.playMode = action.playMode
            }

            is PlayerAction.AddToNext -> {
                val item = browser.getItem(action.mediaId).await().value ?: return@launch
                val index = browser.currentTimeline.indexOf(action.mediaId)

                if (index != -1) {
                    val offset = if (index > browser.currentMediaItemIndex) 1 else 0
                    browser.moveMediaItem(index, browser.currentMediaItemIndex + offset)
                } else {
                    browser.addMediaItem(browser.currentMediaItemIndex + 1, item)
                }
            }

            is PlayerAction.UpdateList -> {
                val index = action.mediaId?.let { action.mediaIds.indexOf(it) }
                    ?.takeIf { it >= 0 }
                    ?: 0

                val items = LMedia.mapItems(action.mediaIds)
                browser.setMediaItems(items, index, 0)
                if (action.start) {
                    browser.play()
                }
            }
        }
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        this@MPlayer.isPlaying = isPlaying
    }

    @OptIn(UnstableApi::class)
    override fun onPlaybackStateChanged(playbackState: Int) {

    }

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        currentMediaItem = mediaItem
        updateItems()

        if (pauseWhenCompletion) {
            browserInstance?.pause()
            pauseWhenCompletion = false
        }
    }

    override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
        currentMediaItem = browserInstance?.currentMediaItem
        currentMediaMetadata = mediaMetadata
        currentDuration = mediaMetadata.durationMs ?: browserInstance?.duration ?: 0L
        // TODO 此处获取到的duration仍然可能是上一首歌曲的时长
    }

    override fun onPlaylistMetadataChanged(mediaMetadata: MediaMetadata) {
        currentPlaylistMetadata = mediaMetadata
    }

    override fun onTimelineChanged(timeline: Timeline, reason: Int) {
        updateItems(timeline)
    }

    fun updateItems(
        timeline: Timeline? = browserInstance?.currentTimeline,
        currentIndex: Int = browserInstance?.currentMediaItemIndex ?: 0
    ) {
        val items = timeline?.toMediaItems() ?: emptyList()
        currentTimelineItems = items.drop(currentIndex) + items.take(currentIndex)

        val ids = currentTimelineItems.map { it.mediaId }
        saveHistoryIds(mediaIds = ids)
    }
}

private fun Timeline.toMediaItems(): List<MediaItem> {
    return (0 until this.windowCount)
        .mapNotNull { this.getWindow(it, Timeline.Window()).mediaItem }
}

private fun Timeline.indexOf(mediaId: String): Int {
    return (0 until this.windowCount).firstOrNull {
        this.getWindow(it, Timeline.Window())
            .mediaItem.mediaId == mediaId
    } ?: -1
}