package com.lalilu.lalbum.screen

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.gigamole.composefadingedges.FadingEdgesGravity
import com.gigamole.composefadingedges.content.FadingEdgesContentType
import com.gigamole.composefadingedges.content.scrollconfig.FadingEdgesScrollConfig
import com.gigamole.composefadingedges.fill.FadingEdgesFillType
import com.gigamole.composefadingedges.verticalFadingEdges
import com.lalilu.component.base.smartBarPadding
import com.lalilu.component.base.songs.SongsScreenStickyHeader
import com.lalilu.component.card.SongCard
import com.lalilu.component.extension.ItemRecorder
import com.lalilu.component.extension.rememberLazyListAnimateScroller
import com.lalilu.component.extension.startRecord
import com.lalilu.component.navigation.AppRouter
import com.lalilu.lalbum.viewModel.AlbumDetailEvent
import com.lalilu.lmedia.entity.LAlbum
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmedia.extension.GroupIdentity
import com.lalilu.lplayer.action.MediaControl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.emptyFlow

@Composable
fun AlbumDetailScreenContent(
    album: LAlbum? = null,
    songs: Map<GroupIdentity, List<LSong>> = emptyMap(),
    eventFlow: Flow<AlbumDetailEvent> = emptyFlow(),
    keys: () -> Collection<Any> = { emptyList() },
    recorder: ItemRecorder = ItemRecorder(),
    isSelecting: () -> Boolean = { false },
    isSelected: (LSong) -> Boolean = { false },
    onSelect: (LSong) -> Unit = {},
    onClickGroup: (GroupIdentity) -> Unit = {}
) {
    val listState = rememberLazyListState()
    val statusBar = WindowInsets.statusBars
    val density = LocalDensity.current
    val stickyHeaderContentType = remember { "group" }
    val scroller = rememberLazyListAnimateScroller(
        listState = listState,
        keys = keys
    )

    LaunchedEffect(Unit) {
        eventFlow.collectLatest { event ->
            when (event) {
                is AlbumDetailEvent.ScrollToItem -> {
                    scroller.animateTo(
                        key = event.key,
                        isStickyHeader = { it.contentType == "group" },
                        offset = { item ->
                            // 若是 sticky header，则滚动到顶部
                            if (item.contentType == "group") {
                                return@animateTo -statusBar.getTop(density)
                            }

                            val closestStickyHeaderSize = listState.layoutInfo.visibleItemsInfo
                                .lastOrNull { it.index < item.index && it.contentType == "group" }
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .statusBarsPadding(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AsyncImage(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .border(
                                width = 1.dp,
                                color = Color.White.copy(0.3f),
                                shape = RoundedCornerShape(8.dp)
                            ),
                        model = album,
                        contentScale = ContentScale.FillWidth,
                        contentDescription = "Album art"
                    )

                    Text(
                        text = album?.name ?: "Unknown",
                        fontSize = 20.sp,
                        lineHeight = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colors.onBackground
                    )
                    Text(
                        text = "共 ${album?.songs?.size ?: 0} 首歌曲",
                        color = MaterialTheme.colors.onBackground.copy(0.6f),
                        fontSize = 12.sp,
                        lineHeight = 12.sp,
                    )
                }
            }

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
                ) {
                    SongCard(
                        song = { it },
                        isSelected = { isSelected(it) },
                        onClick = {
                            if (isSelecting()) {
                                onSelect(it)
                            } else {
                                MediaControl.playWithList(
                                    mediaIds = list.map(LSong::id),
                                    mediaId = it.id
                                )
                            }
                        },
                        onLongClick = {
                            if (isSelecting()) {
                                onSelect(it)
                            } else {
                                AppRouter.route("/pages/songs/detail")
                                    .with("mediaId", it.id)
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