package com.lalilu.lplaylist.screen.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gigamole.composefadingedges.FadingEdgesGravity
import com.gigamole.composefadingedges.content.FadingEdgesContentType
import com.gigamole.composefadingedges.content.scrollconfig.FadingEdgesScrollConfig
import com.gigamole.composefadingedges.fill.FadingEdgesFillType
import com.gigamole.composefadingedges.verticalFadingEdges
import com.lalilu.RemixIcon
import com.lalilu.component.base.NavigatorHeader
import com.lalilu.component.base.smartBarPadding
import com.lalilu.component.base.songs.SongsScreenStickyHeader
import com.lalilu.component.card.SongCard
import com.lalilu.component.extension.ItemRecorder
import com.lalilu.component.extension.rememberLazyListAnimateScroller
import com.lalilu.component.extension.startRecord
import com.lalilu.component.navigation.AppRouter
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmedia.extension.GroupIdentity
import com.lalilu.lplayer.extensions.PlayerAction
import com.lalilu.lplaylist.entity.LPlaylist
import com.lalilu.lplaylist.viewmodel.PlaylistDetailEvent
import com.lalilu.remixicon.Design
import com.lalilu.remixicon.design.editBoxFill
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.emptyFlow
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
internal fun PlaylistDetailScreenContent(
    playlist: LPlaylist? = null,
    songs: Map<GroupIdentity, List<LSong>> = emptyMap(),
    enableDraggable: Boolean = false,
    eventFlow: Flow<PlaylistDetailEvent> = emptyFlow(),
    keys: () -> Collection<Any> = { emptyList() },
    recorder: ItemRecorder = ItemRecorder(),
    isSelecting: () -> Boolean = { false },
    isSelected: (LSong) -> Boolean = { false },
    onSelect: (LSong) -> Unit = {},
    onClickGroup: (GroupIdentity) -> Unit = {},
    onUpdatePlaylist: (List<String>) -> Unit = {}
) {
    val density = LocalDensity.current
    val statusBar = WindowInsets.statusBars
    val hapticFeedback = LocalHapticFeedback.current
    val listState: LazyListState = rememberLazyListState()
    val stickyHeaderContentType = remember { "group" }
    val scroller = rememberLazyListAnimateScroller(
        listState = listState,
        keys = keys
    )

    val playlistState = remember(songs) {
        songs.values.flatten().toMutableStateList()
    }

    val reorderableState = rememberReorderableLazyListState(
        lazyListState = listState
    ) { from, to ->
        playlistState.toMutableList().apply {
            val toIndex = indexOfFirst { it.id == to.key }
            val fromIndex = indexOfFirst { it.id == from.key }
            if (toIndex < 0 || fromIndex < 0) return@rememberReorderableLazyListState

            add(toIndex, removeAt(fromIndex))
            playlistState.clear()
            playlistState.addAll(this)
        }
    }

    LaunchedEffect(Unit) {
        eventFlow.collectLatest { event ->
            when (event) {
                is PlaylistDetailEvent.ScrollToItem -> {
                    scroller.animateTo(
                        key = event.key,
                        isStickyHeader = { it.contentType == stickyHeaderContentType },
                        offset = { item ->
                            // 若是 sticky header，则滚动到顶部
                            if (item.contentType == stickyHeaderContentType) {
                                return@animateTo -statusBar.getTop(density)
                            }

                            val closestStickyHeaderSize = listState.layoutInfo.visibleItemsInfo
                                .lastOrNull { it.index < item.index && it.contentType == stickyHeaderContentType }
                                ?.size ?: 0

                            -(statusBar.getTop(density) + closestStickyHeaderSize)
                        }
                    )
                }

                else -> {}
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .verticalFadingEdges(
                length = statusBar
                    .asPaddingValues()
                    .calculateTopPadding(),
                contentType = FadingEdgesContentType.Dynamic.Lazy.List(
                    scrollConfig = FadingEdgesScrollConfig.Dynamic(),
                    state = listState
                ),
                gravity = FadingEdgesGravity.Start,
                fillType = remember {
                    FadingEdgesFillType.FadeClip(
                        fillStops = Triple(0f, 0.7f, 1f)
                    )
                }
            ),
        state = listState,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.Start
    ) {
        startRecord(recorder) {
            itemWithRecord(key = "HEADER") {
                NavigatorHeader(
                    modifier = Modifier
                        .statusBarsPadding()
                        .fillMaxWidth(),
                    rowExtraSpace = 8.dp,
                    paddingValues = PaddingValues(
                        top = 26.dp,
                        bottom = 20.dp,
                        start = 20.dp,
                        end = 12.dp
                    ),
                    title = playlist?.title ?: "unknown",
                    columnExtraContent = {
                        Text(
                            text = "${playlist?.subTitle}",
                            fontSize = 14.sp,
                            color = contentColorFor(backgroundColor = MaterialTheme.colors.background)
                                .copy(alpha = 0.5f)
                        )
//                        Text(
//                            text = "共 ${playlist?.mediaIds?.size ?: 0} 首歌曲",
//                            fontSize = 14.sp,
//                            color = contentColorFor(backgroundColor = MaterialTheme.colors.background)
//                                .copy(alpha = 0.5f)
//                        )
                    }
                ) {
                    IconButton(onClick = {
                        AppRouter.route("/pages/playlist/edit")
                            .with("playlistId", playlist?.id ?: "")
                            .push()
                    }) {
                        Icon(
                            imageVector = RemixIcon.Design.editBoxFill,
                            contentDescription = null
                        )
                    }
                }
            }

            if (enableDraggable) {
                itemsWithRecord(
                    items = playlistState,
                    key = { it.id },
                    contentType = { it::class.java }
                ) { item ->
                    ReorderableItem(
                        state = reorderableState,
                        key = item.id
                    ) {
                        SongCard(
                            dragModifier = Modifier.draggableHandle(
                                onDragStopped = { onUpdatePlaylist(playlistState.map { it.id }) }
                            ),
                            song = { item },
                            isSelected = { isSelected(item) },
                            onClick = {
                                if (isSelecting()) {
                                    onSelect(item)
                                } else {
                                    PlayerAction.PlayById(item.id).action()
                                }
                            },
                            onLongClick = {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)

                                if (isSelecting()) {
                                    onSelect(item)
                                } else {
                                    AppRouter.route("/pages/songs/detail")
                                        .with("mediaId", item.id)
                                        .jump()
                                }
                            },
                            onEnterSelect = { onSelect(item) }
                        )
                    }
                }
            } else {
                songs.forEach { (group, list) ->
                    if (group !is GroupIdentity.None) {
                        stickyHeaderWithRecord(
                            key = group,
                            contentType = stickyHeaderContentType
                        ) {
                            SongsScreenStickyHeader(
                                modifier = Modifier.animateItem(),
                                listState = listState,
                                group = group,
                                minOffset = { statusBar.getTop(density) },
                                onClickGroup = onClickGroup
                            )
                        }
                    }

                    itemsWithRecord(
                        items = list,
                        key = { it.id },
                        contentType = { it::class.java }
                    ) { item ->
                        SongCard(
                            modifier = Modifier.animateItem(),
                            song = { item },
                            isSelected = { isSelected(item) },
                            onClick = {
                                if (isSelecting()) {
                                    onSelect(item)
                                } else {
                                    PlayerAction.PlayById(item.id).action()
                                }
                            },
                            onLongClick = {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)

                                if (isSelecting()) {
                                    onSelect(item)
                                } else {
                                    AppRouter.route("/pages/songs/detail")
                                        .with("mediaId", item.id)
                                        .jump()
                                }
                            },
                            onEnterSelect = { onSelect(item) }
                        )
                    }
                }
            }
        }

        smartBarPadding()
    }
}