package com.lalilu.lmusic.service

import com.lalilu.common.base.Playable
import com.lalilu.extension_core.ExtensionManager
import com.lalilu.lmedia.LMedia
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmusic.datastore.LastPlayedSp
import com.lalilu.lmusic.utils.extension.moveHeadToTailWithSearch
import com.lalilu.lplayer.runtime.NewRuntime
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
    private val lastPlayedSp: LastPlayedSp
) : CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO
    val playingFlow: Flow<Playable?> = NewRuntime.info.playingIdFlow.flatMapLatest { mediaId ->
        LMedia.getFlow<LSong>(mediaId).flatMapLatest temp@{ playable ->
            if (playable != null || mediaId == null) return@temp flowOf(playable)

            ExtensionManager.requireProviderFlowFromExtensions()
                .flatMapLatest { providers ->
                    providers.firstOrNull { it.isSupported(mediaId) }
                        ?.getPlayableFlowByMediaId(mediaId)
                        ?: flowOf(null)
                }
        }
    }

    val playableFlow: Flow<List<Playable>> = NewRuntime.info.itemsFlow
        .combine(NewRuntime.info.playingIdFlow) { ids, id ->
            id ?: return@combine ids
            ids.moveHeadToTailWithSearch(id) { a, b -> a == b }
        }
        .flatMapLatest { mediaIds ->
            val lMediaResult = LMedia.flowMapBy<LSong>(mediaIds)
            val extensionResult = ExtensionManager.requireProviderFlowFromExtensions()
                .flatMapLatest temp@{ providers ->
                    if (providers.isEmpty()) return@temp flowOf<List<Playable>>(emptyList())

                    val flows = providers.map { it.getPlayableFlowByMediaIds(mediaIds) }
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
        NewRuntime.source = PlayableSource { mediaId ->
            LMedia.get<LSong>(mediaId) ?: ExtensionManager
                .requireProviderFromExtensions()
                .firstNotNullOfOrNull { it.getPlayableByMediaId(mediaId) }
        }

        NewRuntime.info.itemsFlow
            .onEach { lastPlayedSp.lastPlayedListIdsKey.set(it) }
            .launchIn(this)

        NewRuntime.info.playingIdFlow
            .onEach { lastPlayedSp.lastPlayedIdKey.set(it) }
            .launchIn(this)

        NewRuntime.info.positionFlow
            .onEach { lastPlayedSp.lastPlayedPositionKey.set(it) }
            .launchIn(this)
    }
}