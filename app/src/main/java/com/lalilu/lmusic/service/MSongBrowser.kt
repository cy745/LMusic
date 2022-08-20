package com.lalilu.lmusic.service

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaBrowser
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.lalilu.lmedia.entity.items
import com.lalilu.lmedia.indexer.Indexer
import com.lalilu.lmedia.indexer.Library
import com.lalilu.lmusic.manager.GlobalDataManager
import com.lalilu.lmusic.manager.HistoryManager
import com.lalilu.lmusic.utils.safeLaunch
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

@Singleton
class MSongBrowser @Inject constructor(
    @ApplicationContext
    private val mContext: Context,
    private val globalDataManager: GlobalDataManager
) : DefaultLifecycleObserver, CoroutineScope, EnhanceBrowser {
    override val coroutineContext: CoroutineContext = Dispatchers.IO
    private lateinit var browserFuture: ListenableFuture<MediaBrowser>

    private val originPlaylistIds: List<String>
        get() = HistoryManager.currentPlayingIds

    val browser: MediaBrowser?
        get() = if (browserFuture.isDone) browserFuture.get() else null

    @UnstableApi
    override fun onStart(owner: LifecycleOwner) {
        browserFuture = MediaBrowser.Builder(
            mContext, SessionToken(mContext, ComponentName(mContext, MSongService::class.java))
        ).setListener(MyBrowserListener()).buildAsync()
        browserFuture.addListener({ onConnected() }, MoreExecutors.directExecutor())
    }

    override fun onStop(owner: LifecycleOwner) {
        MediaBrowser.releaseFuture(browserFuture)
    }

    private inner class MyBrowserListener : MediaBrowser.Listener {
        override fun onChildrenChanged(
            browser: MediaBrowser,
            parentId: String,
            itemCount: Int,
            params: MediaLibraryService.LibraryParams?
        ) {
            println("[MSongBrowser]#onChildrenChanged")
        }
    }

    private fun onConnected() {
        println("[MSongBrowser]#onConnected")
        val browser = browserFuture.get() ?: return

        if (browser.mediaItemCount == 0 || browser.currentMediaItem == null) {
            recoverLastPlayedData()
        }
    }

    private fun recoverLastPlayedData() = safeLaunch(Dispatchers.IO) {
        val position = HistoryManager.lastPlayedPosition
        val items = HistoryManager.lastPlayedListIds?.let { list ->
            list.mapNotNull { id -> Library.getSongOrNull(id)?.item }
        } ?: Library.getSongs().items()
        ?: emptyList()
        val index = HistoryManager.lastPlayedId?.let { id ->
            items.indexOfFirst { it.mediaId == id }
        }?.coerceAtLeast(0) ?: 0

        withContext(Dispatchers.Main) {
            browser?.setMediaItems(items, index, position)
            browser?.prepare()
        }
    }

    override fun togglePlay(): Boolean {
        when (browser?.isPlaying) {
            true -> browser?.pause()
            false -> browser?.play()
            else -> {}
        }
        return browser?.isPlaying == true
    }

    override fun playByUri(uri: Uri): Boolean {
        // TODO: 需要将uri保存，并可直接从Service读取到该uri
        browser?.apply {
            addMediaItem(currentMediaItemIndex, MediaItem.fromUri(uri))
            seekToDefaultPosition(currentMediaItemIndex)
            prepare()
            play()
        }
        return true
    }

    override fun playById(mediaId: String): Boolean {
        val index = originPlaylistIds.indexOf(mediaId)
        if (index < 0) return false
        browser?.seekToDefaultPosition(index)
        return true
    }

    override fun playById(mediaId: String, playWhenReady: Boolean): Boolean {
        if (playById(mediaId) && playWhenReady) {
            browser?.apply {
                prepare()
                play()
            }
            return true
        }
        return false
    }

    override fun addToNext(mediaId: String): Boolean {
        val currentIndex = browser?.currentMediaItemIndex ?: return false
        if (currentIndex < 0) return false

        val nowIndex = originPlaylistIds.indexOf(mediaId)
        if (currentIndex == nowIndex || (currentIndex + 1) == nowIndex) return false

        if (nowIndex >= 0) {
            val targetIndex = if (nowIndex < currentIndex) currentIndex else currentIndex + 1
            browser?.moveMediaItem(nowIndex, targetIndex)
        } else {
            val item = Library.getSongOrNull(mediaId)?.item ?: return false
            browser?.addMediaItem(currentIndex + 1, item)
        }
        return true
    }

    private var lastRemovedItem: MediaItem? = null
    private var lastRemovedIndex: Int = -1
    private var lastPlayIndex: Int = -1

    override fun removeById(mediaId: String): Boolean {
        browser ?: return false
        return try {
            lastRemovedIndex = originPlaylistIds.indexOf(mediaId)
            if (lastRemovedIndex == browser!!.currentMediaItemIndex) {
                globalDataManager.currentMediaItem.tryEmit(
                    browser!!.getMediaItemAt(browser!!.nextMediaItemIndex)
                )
            }
            lastPlayIndex = browser!!.currentMediaItemIndex
            lastRemovedItem = browser!!.getMediaItemAt(lastRemovedIndex)
            browser!!.removeMediaItem(lastRemovedIndex)
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun revokeRemove(): Boolean {
        if (lastRemovedIndex < 0 || lastRemovedItem == null || browser == null)
            return false

        if (lastRemovedIndex >= browser!!.mediaItemCount) {
            browser!!.addMediaItem(lastRemovedItem!!)
        } else {
            browser!!.addMediaItem(lastRemovedIndex, lastRemovedItem!!)
        }

        if (lastPlayIndex == lastRemovedIndex) {
            browser!!.seekToDefaultPosition(lastPlayIndex)
        }
        return true
    }

    override fun moveByDelta(mediaId: String, delta: Int): Boolean {
        return try {
            val index = originPlaylistIds.indexOf(mediaId)
            browser?.moveMediaItem(index, index + delta)
            true
        } catch (e: Exception) {
            false
        }
    }
}