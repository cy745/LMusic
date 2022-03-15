package com.lalilu.lmusic.service

import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaBrowser
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.SessionToken
import com.blankj.utilcode.util.GsonUtils
import com.google.common.reflect.TypeToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.lalilu.lmusic.Config
import com.lalilu.lmusic.datasource.ALL_ID
import com.lalilu.lmusic.datasource.BaseMediaSource
import com.lalilu.lmusic.datasource.ITEM_PREFIX
import com.lalilu.lmusic.event.GlobalViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

interface EnhanceBrowser {
    fun playByUri(uri: Uri): Boolean
    fun playById(mediaId: String): Boolean
    fun addToNext(mediaId: String): Boolean
    fun removeById(mediaId: String): Boolean
    fun moveByDelta(mediaId: String, delta: Int): Boolean
}

@Singleton
@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class MSongBrowser @Inject constructor(
    @ApplicationContext
    private val mContext: Context,
    private val mGlobal: GlobalViewModel,
    private val mediaSource: BaseMediaSource
) : DefaultLifecycleObserver, CoroutineScope, EnhanceBrowser {
    override val coroutineContext: CoroutineContext = Dispatchers.IO
    private lateinit var browserFuture: ListenableFuture<MediaBrowser>

    private val lastPlayedSp: SharedPreferences by lazy {
        mContext.getSharedPreferences(Config.LAST_PLAYED_SP, Context.MODE_PRIVATE)
    }

    val browser: MediaBrowser?
        get() = if (browserFuture.isDone) browserFuture.get() else null

    var originPlaylistIds: List<String> = emptyList()

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

        if (browser.mediaItemCount == 0) {
            recoverLastPlayedItem()
        }

        browser.addListener(object : Player.Listener {
            override fun onTimelineChanged(timeline: Timeline, reason: Int) {
                val ids = MutableList(browser.mediaItemCount) {
                    return@MutableList browser.getMediaItemAt(it).mediaId
                }

                launch(Dispatchers.IO) {
                    originPlaylistIds = ids
                    mGlobal.currentPlaylist.emit(ids.mapNotNull {
                        mediaSource.getItemById(
                            ITEM_PREFIX + it
                        )
                    })
                }
            }
        })
    }

    fun recoverLastPlayedItem() =
        launch(Dispatchers.IO) {
            val items = recoverLastPlayedList()
            val index = lastPlayedSp.getString(Config.LAST_PLAYED_ID, null)?.let { id ->
                items.indexOfFirst { it.mediaId == id }
            }?.coerceAtLeast(0) ?: 0
            withContext(Dispatchers.Main) {
                browser?.setMediaItems(items)
                browser?.repeatMode = Player.REPEAT_MODE_ALL
                browser?.seekToDefaultPosition(index)
                browser?.prepare()
            }
        }

    private suspend fun recoverLastPlayedList(): List<MediaItem> =
        withContext(Dispatchers.IO) {
            lastPlayedSp.getString(Config.LAST_PLAYED_LIST, null)?.let { json ->
                val typeToken = object : TypeToken<List<String>>() {}
                return@withContext GsonUtils.fromJson<List<String>>(json, typeToken.type)
                    .mapNotNull { mediaSource.getItemById(ITEM_PREFIX + it) }
            }
            return@withContext mediaSource.getChildren(ALL_ID) ?: emptyList()
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
        browser?.seekToDefaultPosition(index)
        return index >= 0
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
            val item = mediaSource.getItemById(ITEM_PREFIX + mediaId) ?: return false
            browser?.addMediaItem(currentIndex + 1, item)
        }
        return true
    }

    override fun removeById(mediaId: String): Boolean {
        browser ?: return false
        return try {
            val index = originPlaylistIds.indexOf(mediaId)
            if (index == browser!!.currentMediaItemIndex) {
                mGlobal.currentMediaItem.tryEmit(
                    browser!!.getMediaItemAt(browser!!.nextMediaItemIndex)
                )
            }
            browser!!.removeMediaItem(index)
            true
        } catch (e: Exception) {
            false
        }
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