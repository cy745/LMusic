package com.lalilu.lmusic.service

import com.lalilu.common.base.Playable
import com.lalilu.extension_core.ExtensionManager
import com.lalilu.lmedia.LMedia
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmusic.datastore.LastPlayedSp
import com.lalilu.lmusic.utils.extension.moveHeadToTailWithSearch
import com.lalilu.lplayer.runtime.PlayableRuntime
import com.lalilu.lplayer.runtime.Runtime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlin.coroutines.CoroutineContext

/**
 * 全局单例，专门用于解决Service和Activity之间复杂的数据交互问题
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LMusicRuntime(
    private val lastPlayedSp: LastPlayedSp
) : PlayableRuntime(), Runtime.Listener, CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Default
    override var listener: Runtime.Listener? = this

    private val shuffleIgnoreHistoryCount = 20
    private val shuffleRetryCount = 5

    val playingFlow: Flow<Playable?> = playingIdFlow.flatMapLatest { mediaId ->
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

    val playableFlow: Flow<List<Playable>> = songsIdsFlow
        .combine(playingIdFlow) { ids, id ->
            id ?: return@combine ids
            ids.moveHeadToTailWithSearch(id) { a, b -> a == b }
        }
        .flatMapLatest { mediaIds ->
            val extensionResult = ExtensionManager.requireProviderFlowFromExtensions()
                .flatMapLatest { providers ->
                    val flows = providers.map { it.getPlayableFlowByMediaIds(mediaIds) }
                    combine(flows) { it.toList().flatten() }
                }

            val lMediaResult = LMedia.flowMapBy<LSong>(mediaIds)

            combine(extensionResult, lMediaResult) { flowResult ->
                flowResult.toList()
                    .flatten()
                    .filter { mediaIds.indexOf(it.mediaId) >= 0 }
                    .sortedBy { mediaIds.indexOf(it.mediaId) }
            }
        }

    override fun getItemById(mediaId: String?): Playable? {
        mediaId ?: return null

        return ExtensionManager
            .requireProviderFromExtensions()
            .firstNotNullOfOrNull { it.getPlayableByMediaId(mediaId) }
            ?: LMedia.get<LSong>(mediaId)
    }

    override fun getShuffle(): Playable? {
        val songIds = songsIdsFlow.value
        val playingIndex = getPlayingIndex()
        val endIndex = playingIndex + shuffleIgnoreHistoryCount
        var targetIndex: Int? = null
        var retryCount = shuffleRetryCount

        if (songIds.size <= shuffleIgnoreHistoryCount * 2) {
            while (true) {
                targetIndex = songIds.indices.randomOrNull() ?: break
                if (targetIndex != playingIndex || retryCount-- <= 0) break
            }
        } else {
            var targetRange = songIds.indices - playingIndex.rangeTo(endIndex)

            if (endIndex >= songIds.size) {
                targetRange = targetRange - 0.rangeTo(endIndex - songIds.size)
            }

            targetIndex = targetRange.randomOrNull()
        }

        targetIndex ?: return null
        return getItemById(songIds[targetIndex])
    }

    override fun onSongsUpdate(songsIds: List<String>) {
        lastPlayedSp.lastPlayedListIdsKey.set(songsIds)
    }

    override fun onPlayingUpdate(songId: String?) {
        lastPlayedSp.lastPlayedIdKey.set(songId)
    }

    override fun onPositionUpdate(position: Long) {
        lastPlayedSp.lastPlayedPositionKey.set(position)
    }
}