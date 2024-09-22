package com.lalilu.lalbum.viewModel

import androidx.compose.runtime.mutableStateOf
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.lalilu.component.base.songs.ItemSearcher
import com.lalilu.component.base.songs.ItemSorter
import com.lalilu.component.extension.toState
import com.lalilu.lmedia.LMedia
import com.lalilu.lmedia.entity.LAlbum
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmedia.extension.GroupIdentity
import com.lalilu.lmedia.extension.ListAction
import com.lalilu.lmedia.extension.SortStaticAction
import com.lalilu.lplayer.LPlayer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

internal sealed interface AlbumsScreenAction {
    data object LocaleToPlayingItem : AlbumsScreenAction
    data class LocaleToGroupItem(val item: GroupIdentity) : AlbumsScreenAction
}

internal sealed interface AlbumsScreenEvent {
    data class ScrollToItem(val key: Any) : AlbumsScreenEvent
}

internal class AlbumsSM(
    val albumsIds: List<String> = emptyList()
) : ScreenModel {
    // 持久化元素的状态
    val showSortPanel = mutableStateOf(false)
    val showJumperDialog = mutableStateOf(false)
    val showSearcherPanel = mutableStateOf(false)
    val showTitle = mutableStateOf(false)
    val supportSortActions = setOf<ListAction?>(
        SortStaticAction.Normal,
        SortStaticAction.Title,
        SortStaticAction.ItemsCount,
        SortStaticAction.Duration,
        SortStaticAction.AddTime,
        SortStaticAction.Shuffle,
    ).filterNotNull()
        .toSet()

    // 数据流
    private fun flow(): Flow<List<LAlbum>> {
        return if (albumsIds.isEmpty()) LMedia.getFlow<LAlbum>()
        else LMedia.flowMapBy<LAlbum>(albumsIds)
    }

    val searcher = ItemSearcher(flow())
    val sorter = ItemSorter(searcher.output, supportSortActions)
    val albums = sorter.output.toState(
        defaultValue = emptyMap(),
        scope = screenModelScope,
    )


    private val _eventFlow = MutableSharedFlow<AlbumsScreenEvent>()
    val eventFlow: SharedFlow<AlbumsScreenEvent> = _eventFlow


    fun doAction(action: AlbumsScreenAction) = screenModelScope.launch {
        when (action) {
            AlbumsScreenAction.LocaleToPlayingItem -> {
                // 获取正在播放的元素ID
                val mediaId = LPlayer.runtime.info.playingIdFlow.value
                    ?: return@launch

                // 获取该元素
                val item = LMedia.get<LSong>(mediaId)
                    ?: return@launch

                // 获取该元素的所属分组ID
                val artistsIds = item.artists
                    .map { it.id }
                    .takeIf { it.isNotEmpty() }
                    ?: return@launch

                // 获取第一个存在与列表中的元素的Index
//                val list = recorder.list()
//                artistsIds.firstOrNull { list.contains(it) }?.let {
//                    _eventFlow.emit(AlbumsScreenEvent.ScrollToItem(it))
//                }
            }

            is AlbumsScreenAction.LocaleToGroupItem -> {
                _eventFlow.emit(AlbumsScreenEvent.ScrollToItem(action.item))
            }

            else -> {}
        }
    }
}