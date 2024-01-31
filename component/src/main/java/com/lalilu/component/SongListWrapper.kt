package com.lalilu.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Chip
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.lalilu.common.base.Playable
import com.lalilu.component.card.SongCard
import com.lalilu.component.extension.ItemSelectHelper
import com.lalilu.component.extension.LazyListScrollToHelper
import com.lalilu.component.extension.rememberFixedStatusBarHeightDp
import com.lalilu.component.extension.rememberStickyHelper
import com.lalilu.component.extension.stickyHeaderExtent
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyColumnState


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
fun <K : Any> SongListWrapper(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    itemSelectHelper: () -> ItemSelectHelper? = { null },
    scrollToHelper: () -> LazyListScrollToHelper? = { null },
    idMapper: (K) -> String,
    itemsMap: Map<K, List<Playable>>,
    onClickItem: (Playable) -> Unit = {},
    onLongClickItem: (Playable) -> Unit = {},
    onDoubleClickItem: (Playable) -> Unit = {},
    onHeaderClick: (Any) -> Unit = {},
    hasLyric: (Playable) -> Boolean = { false },
    isFavourite: (Playable) -> Boolean = { false },
    isItemPlaying: (Playable) -> Boolean = { false },
    showPrefixContent: () -> Boolean = { false },
    emptyContent: @Composable () -> Unit = {},
    prefixContent: @Composable (item: Playable) -> Unit = {},
    headerContent: LazyListScope.() -> Unit,
    footerContent: LazyListScope.() -> Unit,
) {
    val haptic = LocalHapticFeedback.current

    val scrollHelper = remember { scrollToHelper() }
    val selector = remember { itemSelectHelper() }
    val stickyHelper = rememberStickyHelper(
        listState = state,
        contentType = { "GroupIdentity::class" }
    )

    LLazyColumn(
        modifier = modifier,
        state = state,
        contentPadding = PaddingValues(top = rememberFixedStatusBarHeightDp()),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        headerContent()
        scrollHelper?.startRecord()

        if (!itemsMap.values.any { it.isNotEmpty() }) {
            scrollHelper?.doRecord("EMPTY_CONTENT")
            item(key = "EMPTY_CONTENT") { emptyContent() }
        } else {
            for ((key, list) in itemsMap) {
                val headerTitle = idMapper(key)

                if (headerTitle != "") {
                    scrollHelper?.doRecord(key)
                    stickyHeaderExtent(
                        helper = stickyHelper,
                        key = { key }
                    ) {
                        Chip(
                            modifier = Modifier
                                .animateItemPlacement()
                                .offsetWithHelper()
                                .zIndexWithHelper(),
                            onClick = { onHeaderClick(key) }
                        ) {
                            Text(
                                style = MaterialTheme.typography.h6,
                                text = headerTitle
                            )
                        }
                    }
                }

                scrollHelper?.doRecord(list.map { it.mediaId })
                items(
                    items = list,
                    key = { it.mediaId },
                    contentType = { Playable::class }
                ) { item ->
                    SongCard(
                        song = { item },
                        modifier = Modifier.animateItemPlacement(),
                        onClick = {
                            if (selector?.isSelecting() == true) {
                                selector.onSelect(item)
                            } else {
                                onClickItem(item)
                            }
                        },
                        onLongClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onLongClickItem(item)
                        },
                        onDoubleClick = {
                            if (selector?.isSelecting() != true) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onDoubleClickItem(item)
                            }
                        },
                        onEnterSelect = { selector?.onSelect(item) },
                        isSelected = { selector?.isSelected(item) ?: false },
                        isPlaying = { isItemPlaying(item) },
                        showPrefix = showPrefixContent,
                        hasLyric = { hasLyric(item) },
                        prefixContent = { modifier ->
                            Row(
                                modifier = modifier
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colors.surface)
                                    .padding(start = 4.dp, end = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                prefixContent(item)
                            }
                        }
                    )
                }
            }
        }
        scrollHelper?.endRecord()

        footerContent()
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReorderableSongListWrapper(
    modifier: Modifier = Modifier,
    items: List<Playable>,
    listState: LazyListState = rememberLazyListState(),
    onDragMoveEnd: (List<Playable>) -> Unit,
    itemSelectHelper: () -> ItemSelectHelper? = { null },
    scrollToHelper: () -> LazyListScrollToHelper? = { null },
    onClickItem: (Playable) -> Unit = {},
    onLongClickItem: (Playable) -> Unit = {},
    onDoubleClickItem: (Playable) -> Unit = {},
    hasLyric: (Playable) -> Boolean = { false },
    isFavourite: (Playable) -> Boolean = { false },
    isItemPlaying: (Playable) -> Boolean = { false },
    showPrefixContent: () -> Boolean = { false },
    emptyContent: @Composable () -> Unit = {},
    prefixContent: @Composable (item: Playable) -> Unit = {},
    headerContent: LazyListScope.() -> Unit,
    footerContent: LazyListScope.() -> Unit,
) {
    val haptic = LocalHapticFeedback.current

    val scrollHelper = remember { scrollToHelper() }
    val selector = remember { itemSelectHelper() }
    val itemsState = remember(items) { items.toMutableStateList() }

    val reorderableState = rememberReorderableLazyColumnState(
        lazyListState = listState
    ) { from, to ->
        itemsState.toMutableList().apply {
            val toIndex = indexOfFirst { it.mediaId == to.key }
            val fromIndex = indexOfFirst { it.mediaId == from.key }
            if (toIndex < 0 || fromIndex < 0) return@rememberReorderableLazyColumnState

            add(toIndex, removeAt(fromIndex))
            itemsState.clear()
            itemsState.addAll(this)
        }
    }

    LLazyColumn(
        modifier = modifier,
        state = listState,
        contentPadding = PaddingValues(top = rememberFixedStatusBarHeightDp()),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        headerContent()
        scrollHelper?.startRecord()

        if (itemsState.isEmpty()) {
            scrollHelper?.doRecord("EMPTY_CONTENT")
            item(key = "EMPTY_CONTENT") {
                emptyContent()
            }
        } else {
            scrollHelper?.doRecord(itemsState.map { it.mediaId })
            items(
                items = itemsState,
                key = { it.mediaId },
                contentType = { Playable::class }
            ) { item ->
                ReorderableItem(
                    reorderableLazyListState = reorderableState,
                    key = item.mediaId
                ) { isDragging ->
                    SongCard(
                        song = { item },
                        modifier = Modifier.animateItemPlacement(),
                        dragModifier = Modifier.draggableHandle(
                            onDragStopped = { onDragMoveEnd(itemsState) }
                        ),
                        onClick = {
                            if (selector?.isSelecting() == true) {
                                selector.onSelect(item)
                            } else {
                                onClickItem(item)
                            }
                        },
                        onLongClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onLongClickItem(item)
                        },
                        onDoubleClick = {
                            if (selector?.isSelecting() != true) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onDoubleClickItem(item)
                            }
                        },
                        onEnterSelect = { selector?.onSelect(item) },
                        isSelected = { selector?.isSelected(item) ?: false },
                        isPlaying = { isItemPlaying(item) },
                        showPrefix = showPrefixContent,
                        hasLyric = { hasLyric(item) },
                        prefixContent = { modifier ->
                            Row(
                                modifier = modifier
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colors.surface)
                                    .padding(start = 4.dp, end = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                prefixContent(item)
                            }
                        }
                    )
                }
            }
        }


        scrollHelper?.endRecord()
        footerContent()
    }
}