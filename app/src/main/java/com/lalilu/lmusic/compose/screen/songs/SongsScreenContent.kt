package com.lalilu.lmusic.compose.screen.songs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gigamole.composefadingedges.FadingEdgesGravity
import com.gigamole.composefadingedges.content.FadingEdgesContentType
import com.gigamole.composefadingedges.content.scrollconfig.FadingEdgesScrollConfig
import com.gigamole.composefadingedges.fill.FadingEdgesFillType
import com.gigamole.composefadingedges.verticalFadingEdges
import com.lalilu.common.base.Playable
import com.lalilu.component.base.smartBarPadding
import com.lalilu.component.card.SongCard
import com.lalilu.component.extension.StickyHeaderOffsetHelper
import com.lalilu.component.extension.startRecord
import com.lalilu.component.navigation.AppRouter
import com.lalilu.lmedia.extension.GroupIdentity
import com.lalilu.lplayer.extensions.PlayerAction
import kotlinx.coroutines.flow.collectLatest
import my.nanihadesuka.compose.InternalLazyColumnScrollbar
import my.nanihadesuka.compose.ScrollbarSelectionMode
import my.nanihadesuka.compose.ScrollbarSettings

@Composable
internal fun SongsScreenContent(
    songsSM: SongsSM,
    isSelecting: () -> Boolean = { false },
    isSelected: (Playable) -> Boolean = { false },
    onSelect: (Playable) -> Unit = {},
    onClickGroup: (GroupIdentity) -> Unit = {}
) {
    val density = LocalDensity.current
    val hapticFeedback = LocalHapticFeedback.current
    val listState: LazyListState = rememberLazyListState()
    val statusBar = WindowInsets.statusBars
    val songs by songsSM.songs

    LaunchedEffect(Unit) {
        songsSM.event().collectLatest { event ->
            when (event) {
                is SongsScreenEvent.ScrollToItem -> {
                    val targetIndex = event.index
                    var maxScrollCount = 3

                    // 限制最多滚动3次，避免无限死循环
                    while (maxScrollCount-- > 0) {
                        val targetItem = listState.layoutInfo.visibleItemsInfo
                            .firstOrNull { it.index == targetIndex }

                        // 判断元素是否在可见范围内
                        if (targetItem != null) {
                            val isStickHeader = targetItem.contentType == "group"

                            if (isStickHeader) {
                                val isFirstGroupItem = listState.layoutInfo.visibleItemsInfo
                                    .firstOrNull { it.contentType == "group" }
                                    ?.index == targetIndex

                                if (isFirstGroupItem) {
                                    listState.scrollToItem(
                                        index = targetIndex,
                                        scrollOffset = -statusBar.getTop(density)
                                    )
                                } else {
                                    listState.animateScrollToItem(
                                        index = targetIndex,
                                        scrollOffset = -statusBar.getTop(density)
                                    )
                                }
                            } else {
                                val lastGroupItemOffset = listState.layoutInfo.visibleItemsInfo
                                    .lastOrNull { it.contentType == "group" && it.index < targetIndex }
                                    ?.size ?: 0

                                listState.animateScrollToItem(
                                    index = targetIndex,
                                    scrollOffset = -(statusBar.getTop(density) + lastGroupItemOffset)
                                )
                            }
                            break
                        } else {
                            listState.scrollToItem(
                                index = targetIndex,
                                scrollOffset = -statusBar.getTop(density)
                            )
                        }
                    }
                }
            }
        }
    }

    SongsScreenScrollBar(
        modifier = Modifier.fillMaxSize(),
        listState = listState
    ) {
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
        ) {
            startRecord(songsSM.recorder) {
                itemWithRecord(key = "全部歌曲") {
                    val count = remember(songs) { songs.values.flatten().size }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .statusBarsPadding(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "全部歌曲",
                            fontSize = 20.sp,
                            lineHeight = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colors.onBackground
                        )
                        Text(
                            text = "共 $count 首歌曲",
                            color = MaterialTheme.colors.onBackground.copy(0.6f),
                            fontSize = 12.sp,
                            lineHeight = 12.sp,
                        )
                    }
                }

                songs.forEach { (group, list) ->
                    stickyHeaderWithRecord(
                        key = group,
                        contentType = "group"
                    ) {
                        SongsScreenStickyHeader(
                            listState = listState,
                            group = group,
                            minOffset = { statusBar.getTop(density) },
                            onClickGroup = onClickGroup
                        )
                    }

                    itemsWithRecord(
                        items = list,
                        key = { it.mediaId },
                        contentType = { it::class.java }
                    ) {
                        SongCard(
                            song = { it },
                            isSelected = { isSelected(it) },
                            onClick = {
                                if (isSelecting()) {
                                    onSelect(it)
                                } else {
                                    PlayerAction.PlayById(it.mediaId).action()
                                }
                            },
                            onLongClick = {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)

                                if (isSelecting()) {
                                    onSelect(it)
                                } else {
                                    AppRouter.route("/pages/songs/detail")
                                        .with("mediaId", it.mediaId)
                                        .jump()
                                }
                            },
                            onEnterSelect = { onSelect(it) }
                        )
                    }
                }
            }

            smartBarPadding()
        }
    }
}

@Composable
fun SongsScreenStickyHeader(
    modifier: Modifier = Modifier,
    listState: LazyListState,
    group: GroupIdentity,
    minOffset: () -> Int,
    onClickGroup: (GroupIdentity) -> Unit
) {
    StickyHeaderOffsetHelper(
        modifier = modifier,
        key = group,
        listState = listState,
        minOffset = minOffset,
    ) { modifierFromHelper, isFloating ->
        Box(
            modifier = modifierFromHelper
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .widthIn(min = 64.dp)
                .height(IntrinsicSize.Max)
                .clip(RoundedCornerShape(8.dp))
                .clickable { onClickGroup(group) }
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colors.onBackground.copy(0.1f),
                    shape = RoundedCornerShape(8.dp)
                )
                .background(color = MaterialTheme.colors.background)
        ) {
            Text(
                modifier = Modifier.padding(
                    horizontal = 16.dp,
                    vertical = 12.dp
                ),
                textAlign = TextAlign.Start,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                lineHeight = 14.sp,
                text = group.text
            )

            Spacer(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxHeight()
                    .padding(vertical = 12.dp)
                    .padding(start = 6.dp)
                    .width(2.dp)
                    .clip(RoundedCornerShape(50))
                    .drawBehind { drawRect(color = Color(0xFF0088FF)) }
            )
        }
    }
}

@Composable
fun SongsScreenScrollBar(
    modifier: Modifier = Modifier,
    listState: LazyListState,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        content()

        InternalLazyColumnScrollbar(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight(0.5f),
            state = listState,
            settings = ScrollbarSettings(
                alwaysShowScrollbar = true,
                scrollbarPadding = 4.dp,
                thumbMinLength = 0.2f,
                selectionMode = ScrollbarSelectionMode.Full,
                thumbUnselectedColor = MaterialTheme.colors.onBackground.copy(0.2f),
                thumbSelectedColor = MaterialTheme.colors.onBackground.copy(0.8f),
            ),
            indicatorContent = { index, isThumbSelected ->
                AnimatedVisibility(
                    modifier = Modifier.offset(x = (-20).dp),
                    enter = fadeIn() + scaleIn(initialScale = 0.5f),
                    exit = fadeOut() + scaleOut(targetScale = 0.5f),
                    visible = isThumbSelected
                ) {
                    Text(
                        modifier = Modifier
                            .widthIn(min = 100.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colors.onBackground.copy(0.1f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .background(color = MaterialTheme.colors.background)
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        textAlign = TextAlign.End,
                        text = "$index",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        lineHeight = 14.sp
                    )
                }
            }
        )
    }
}