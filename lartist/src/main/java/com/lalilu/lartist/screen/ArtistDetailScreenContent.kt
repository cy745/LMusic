package com.lalilu.lartist.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsIgnoringVisibility
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lalilu.component.base.smartBarPadding
import com.lalilu.component.base.songs.SongsScreenStickyHeader
import com.lalilu.component.card.SongCard
import com.lalilu.component.extension.ItemRecorder
import com.lalilu.component.extension.SortExtraPresetUI
import com.lalilu.component.extension.fadeEdgeForStatusBar
import com.lalilu.component.extension.rememberLazyListAnimateScroller
import com.lalilu.component.extension.startRecord
import com.lalilu.component.extension.statusBarsIgnoringVisibilityPadding
import com.lalilu.component.navigation.AppRouter
import com.lalilu.component.navigation.NavIntent
import com.lalilu.component.state
import com.lalilu.lartist.component.ArtistCard
import com.lalilu.lartist.viewModel.ArtistDetailEvent
import com.lalilu.lmedia.entity.LArtist
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmedia.extension.sortable.GroupId
import com.lalilu.lmedia.extension.sortable.SortResult
import com.lalilu.lplayer.MPlayer
import com.lalilu.lplayer.action.MediaControl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.emptyFlow

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ArtistDetailScreenContent(
    artist: LArtist? = null,
    songs: SortResult<LSong> = SortResult.empty(),
    eventFlow: Flow<ArtistDetailEvent> = emptyFlow(),
    keys: () -> Collection<Any> = { emptyList() },
    recorder: ItemRecorder = ItemRecorder(),
    isSelecting: () -> Boolean = { false },
    isSelected: (LSong) -> Boolean = { false },
    onSelect: (LSong) -> Unit = {},
    onClickGroup: (GroupId) -> Unit = {}
) {
    val listState = rememberLazyListState()
    val statusBar = WindowInsets.statusBarsIgnoringVisibility
    val density = LocalDensity.current
    val stickyHeaderContentType = remember { "group" }
    val favouriteIds = state("favourite_ids", emptyList<String>())
    val scroller = rememberLazyListAnimateScroller(
        listState = listState,
        keys = keys
    )

    val relateArtist = remember(artist) {
        artist?.songs?.map { it.artists }
            ?.flatten()
            ?.toSet()
            ?.filter { it.id != artist.name }
            ?.toList()
            ?: emptyList()
    }

    LaunchedEffect(Unit) {
        eventFlow.collectLatest { event ->
            when (event) {
                is ArtistDetailEvent.ScrollToItem -> {
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
            .fadeEdgeForStatusBar(),
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
                        .statusBarsIgnoringVisibilityPadding(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = artist?.name ?: "Unknown",
                        fontSize = 20.sp,
                        lineHeight = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colors.onBackground
                    )
                    Text(
                        text = "共 ${artist?.songs?.size ?: 0} 首歌曲",
                        color = MaterialTheme.colors.onBackground.copy(0.6f),
                        fontSize = 12.sp,
                        lineHeight = 12.sp,
                    )
                }
            }

            songs.draw {
                groupId?.let { groupId ->
                    stickyHeaderWithRecord(
                        key = groupId,
                        contentType = stickyHeaderContentType
                    ) {
                        SongsScreenStickyHeader(
                            modifier = Modifier.animateItem(),
                            listState = listState,
                            group = groupId,
                            minOffset = { statusBar.getTop(density) },
                            onClickGroup = onClickGroup
                        )
                    }
                }

                itemsIndexedWithRecord(
                    items = items,
                    key = { index, item -> item.id },
                    contentType = { index, item -> item::class.java }
                ) { index, item ->
                    val extra = extras.getOrNull(index)

                    SongCard(
                        song = { item },
                        onClick = {
                            if (isSelecting()) {
                                onSelect(item)
                            } else {
                                MediaControl.playWithList(
                                    mediaIds = songs.itemList.map(LSong::id),
                                    mediaId = item.id
                                )
                            }
                        },
                        onLongClick = {
                            if (isSelecting()) {
                                onSelect(item)
                            } else {
                                AppRouter.route("/pages/songs/detail")
                                    .with("mediaId", item.id)
                                    .jump()
                            }
                        },
                        onEnterSelect = { onSelect(item) },
                        isFavour = { favouriteIds.value.contains(item.id) },
                        isSelected = { isSelected(item) },
                        prefixContent = { SortExtraPresetUI.Show(extra) }
                    )
                }
            }

            if (relateArtist.isNotEmpty()) {
                itemWithRecord(key = "EXTRA_HEADER") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .statusBarsPadding(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "相关艺术家",
                            fontSize = 20.sp,
                            lineHeight = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colors.onBackground
                        )
                    }
                }

                itemsIndexedWithRecord(
                    items = relateArtist,
                    key = { _, item -> item.id },
                    contentType = { _, _ -> LArtist::class }
                ) { index, item ->
                    ArtistCard(
                        modifier = Modifier.animateItem(),
                        title = item.name,
                        subTitle = "#$index",
                        songCount = item.songs.size.toLong(),
                        imageSource = { item.songs.firstOrNull() },
                        isPlaying = { item.songs.any { MPlayer.isItemPlaying(it.id) } },
                        onClick = { AppRouter.intent(NavIntent.Push(ArtistDetailScreen(item.id))) }
                    )
                }
            }
        }

        smartBarPadding()
    }
}