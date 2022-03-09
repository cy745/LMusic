package com.lalilu.lmusic.service

import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
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
import com.lalilu.lmusic.datasource.BaseMediaSource
import com.lalilu.lmusic.datasource.ITEM_PREFIX
import com.lalilu.lmusic.event.GlobalViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

interface EnhanceBrowser {
    fun playById(mediaId: String): Boolean
    fun addToNext(mediaId: String): Boolean
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

    private val playerSp: SharedPreferences by lazy {
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

        if (browser.currentMediaItem == null) {
            recoverLastPlayedItem(browser, recoverLastPlayedList(browser))
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

    private fun recoverLastPlayedItem(browser: MediaBrowser, ids: List<String>) {
        playerSp.getString(Config.LAST_PLAYED_ID, null)?.let { id ->
            val index = ids.indexOfFirst { it == id }
            if (index >= 0) {
                browser.repeatMode = Player.REPEAT_MODE_ALL
                browser.seekToDefaultPosition(index)
                browser.prepare()
            }
        }
    }

    private fun recoverLastPlayedList(browser: MediaBrowser): List<String> {
        playerSp.getString(Config.LAST_PLAYED_LIST, null)?.let { json ->
            val typeToken = object : TypeToken<List<String>>() {}
            val list = GsonUtils.fromJson<List<String>>(json, typeToken.type)

            browser.setMediaItems(list.mapNotNull {
                mediaSource.getItemById(ITEM_PREFIX + it)
            })
            return list
        }
        return emptyList()
    }

    override fun playById(mediaId: String): Boolean {
        val index = originPlaylistIds.indexOf(mediaId)
        browser?.seekToDefaultPosition(index)
        return index >= 0
    }

    override fun addToNext(mediaId: String): Boolean {
        val currentIndex = browser?.currentMediaItemIndex ?: return false
        if (currentIndex < 0) return false

        if (originPlaylistIds.contains(mediaId)) {
            val nowIndex = originPlaylistIds.indexOf(mediaId)
            if (nowIndex < 0 || currentIndex == nowIndex) return false

            val targetIndex = if (nowIndex < currentIndex) currentIndex else currentIndex + 1
            browser?.moveMediaItem(nowIndex, targetIndex)
        } else {
            val item = mediaSource.getItemById(ITEM_PREFIX + mediaId) ?: return false
            browser?.addMediaItem(currentIndex + 1, item)
        }
        return true
    }
}