package com.lalilu.lmusic.service

import com.lalilu.common.base.Playable
import com.lalilu.extension_core.ExtensionManager
import com.lalilu.lmedia.LMedia
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmusic.datastore.LastPlayedSp
import com.lalilu.lmusic.utils.extension.moveHeadToTailWithSearch
import com.lalilu.lplayer.LPlayer
import com.lalilu.lplayer.runtime.PlayableSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.coroutines.CoroutineContext

@OptIn(ExperimentalCoroutinesApi::class)
class ExtendRuntime(
    private val lastPlayedSp: LastPlayedSp,
) : CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO
    val playingFlow: Flow<Playable?> = LPlayer.runtime.info.playingIdFlow.flatMapLatest { mediaId ->
        LMedia.getFlow<LSong>(mediaId).flatMapLatest temp@{ playable ->
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

    val playableFlow: Flow<List<Playable>> = LPlayer.runtime.info.itemsFlow
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

    init {
        LPlayer.runtime.source = PlayableSource { mediaId ->
            LMedia.get<LSong>(mediaId) ?: ExtensionManager
                .requireProviderFromExtensions()
                .firstNotNullOfOrNull { it.getById(mediaId) }
        }

        LPlayer.runtime.info.itemsFlow
            .onEach { lastPlayedSp.lastPlayedListIdsKey.set(it) }
            .launchIn(this)

        LPlayer.runtime.info.playingIdFlow
            .onEach { lastPlayedSp.lastPlayedIdKey.set(it) }
            .launchIn(this)

        LPlayer.runtime.info.positionFlow
            .onEach { lastPlayedSp.lastPlayedPositionKey.set(it) }
            .launchIn(this)
    }
}