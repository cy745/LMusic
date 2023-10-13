package com.lalilu.lmusic.service

import android.content.ComponentName
import android.content.Context
import android.support.v4.media.MediaBrowserCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.lalilu.common.base.Playable
import com.lalilu.extension_core.ExtensionManager
import com.lalilu.lmedia.LMedia
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmusic.datastore.LastPlayedSp
import com.lalilu.lmusic.utils.extension.moveHeadToTailWithSearch
import com.lalilu.lplayer.LPlayer
import com.lalilu.lplayer.runtime.ItemSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

@OptIn(ExperimentalCoroutinesApi::class)
class LMusicServiceConnector(
    private val context: Context,
    private val lastPlayedSp: LastPlayedSp,
) : DefaultLifecycleObserver, CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.IO
    private val browser: MediaBrowserCompat by lazy {
        MediaBrowserCompat(
            context,
            ComponentName(context, LMusicService::class.java),
            MediaBrowserCompat.ConnectionCallback(),
            null
        )
    }

    init {
        launch {
            reloadItems()
            initLPlayer()
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        reloadItems()
        browser.connect()
    }

    override fun onStop(owner: LifecycleOwner) {
        browser.disconnect()
    }

    private fun reloadItems() {
        val queue = LPlayer.runtime.queue
        // 若当前播放列表不为空，则不尝试提取历史数据填充
        if (queue.getSize() != 0) {
            return
        }

        val songIds = lastPlayedSp.lastPlayedListIdsKey.get()
        val lastPlayedIdKey = lastPlayedSp.lastPlayedIdKey.get()

        // 存在历史记录
        if (songIds.isNotEmpty()) {
            queue.setIds(songIds)
            queue.setCurrentId(lastPlayedIdKey)
            return
        }

        LMedia.whenReady {
            val songs = LMedia.get<LSong>()
            queue.setIds(songs.map { it.id })
            queue.setCurrentId(songs.getOrNull(0)?.id)
        }
    }

    private fun initLPlayer() {
        LPlayer.runtime.source = object : ItemSource<Playable> {
            override fun getById(id: String): Playable? {
                return LMedia.get<LSong>(id) ?: ExtensionManager
                    .requireProviderFromExtensions()
                    .firstNotNullOfOrNull { it.getById(id) }
            }

            override fun flowMapId(idFlow: Flow<String?>): Flow<Playable?> =
                idFlow.flatMapLatest { mediaId ->
                    LMedia.getFlow<LSong>(mediaId)
                        .flatMapLatest temp@{ playable ->
                            if (mediaId == null) return@temp flowOf(playable)
                            if (playable != null) return@temp flowOf(playable)

                            ExtensionManager.requireProviderFlowFromExtensions()
                                .flatMapLatest { providers ->
                                    providers.firstOrNull { it.isSupported(mediaId) }
                                        ?.getFlowById(mediaId)
                                        ?: flowOf(null)
                                }
                        }
                }


            override fun flowMapIds(idsFlow: Flow<List<String>>): Flow<List<Playable>> = idsFlow
                .combine(LPlayer.runtime.info.playingIdFlow) { ids, id ->
                    id ?: return@combine ids
                    ids.moveHeadToTailWithSearch(id) { a, b -> a == b }
                }
                .flatMapLatest { mediaIds ->
                    val lMediaResult = LMedia.flowMapBy<LSong>(mediaIds)
                    val extensionResult = ExtensionManager.requireProviderFlowFromExtensions()
                        .flatMapLatest temp@{ providers ->
                            if (providers.isEmpty()) return@temp flowOf<List<Playable>>(emptyList())

                            val flows = providers.map { it.getFlowByIds(mediaIds) }
                            combine(flows) { it.toList().flatten() }
                        }

                    combine(extensionResult, lMediaResult) { flowResult ->
                        flowResult.toList()
                            .flatten()
                            .filter { mediaIds.indexOf(it.mediaId) >= 0 }
                            .sortedBy { mediaIds.indexOf(it.mediaId) }
                    }
                }
        }

        launch {
            LPlayer.runtime.info.idsFlow.collectLatest {
                lastPlayedSp.lastPlayedListIdsKey.set(it)
            }
        }

        launch {
            LPlayer.runtime.info.playingIdFlow.collectLatest {
                lastPlayedSp.lastPlayedIdKey.set(it)
            }
        }

        launch {
            LPlayer.runtime.info.positionFlow.collectLatest {
                lastPlayedSp.lastPlayedPositionKey.set(it)
            }
        }
    }
}