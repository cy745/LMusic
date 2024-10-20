package com.lalilu.lartist.screen.artists

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
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gigamole.composefadingedges.FadingEdgesGravity
import com.gigamole.composefadingedges.content.FadingEdgesContentType
import com.gigamole.composefadingedges.content.scrollconfig.FadingEdgesScrollConfig
import com.gigamole.composefadingedges.fill.FadingEdgesFillType
import com.gigamole.composefadingedges.verticalFadingEdges
import com.lalilu.component.base.songs.SongsScreenScrollBar
import com.lalilu.component.base.songs.SongsScreenStickyHeader
import com.lalilu.component.extension.rememberLazyListAnimateScroller
import com.lalilu.component.extension.startRecord
import com.lalilu.component.navigation.AppRouter
import com.lalilu.component.navigation.NavIntent
import com.lalilu.component.viewmodel.IPlayingViewModel
import com.lalilu.lartist.component.ArtistCard
import com.lalilu.lartist.screen.ArtistDetailScreen
import com.lalilu.lartist.viewModel.ArtistsSM
import com.lalilu.lartist.viewModel.ArtistsScreenEvent
import com.lalilu.lmedia.entity.LArtist
import com.lalilu.lmedia.extension.GroupIdentity
import com.lalilu.lplayer.MPlayer
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.koinInject


@Composable
internal fun ArtistsScreenContent(
    artistsSM: ArtistsSM,
    playingVM: IPlayingViewModel = koinInject(),
    isSelecting: () -> Boolean = { false },
    isSelected: (LArtist) -> Boolean = { false },
    onSelect: (LArtist) -> Unit = {},
    onClickGroup: (GroupIdentity) -> Unit = {},
) {
    val artists by artistsSM.artists
    val listState = rememberLazyListState()
    val statusBar = WindowInsets.statusBars
    val density = LocalDensity.current
    val stickyHeaderContentType = remember { "group" }
    val scroller = rememberLazyListAnimateScroller(
        listState = listState,
        keys = { artistsSM.recorder.list().filterNotNull() }
    )

    LaunchedEffect(Unit) {
        artistsSM.eventFlow.collectLatest { event ->
            when (event) {
                is ArtistsScreenEvent.ScrollToItem -> {
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
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.Start
        ) {
            startRecord(artistsSM.recorder) {
                itemWithRecord(key = "艺术家") {
                    val count = remember(artists) { artists.values.flatten().size }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .statusBarsPadding(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "艺术家",
                            fontSize = 20.sp,
                            lineHeight = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colors.onBackground
                        )
                        Text(
                            text = "共 $count 位艺术家",
                            color = MaterialTheme.colors.onBackground.copy(0.6f),
                            fontSize = 12.sp,
                            lineHeight = 12.sp,
                        )
                    }
                }

                artists.forEach { (group, list) ->
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

                    itemsIndexedWithRecord(
                        items = list,
                        key = { _, item -> item.id },
                        contentType = { _, _ -> LArtist::class }
                    ) { index, item ->
                        ArtistCard(
                            modifier = Modifier.animateItem(),
                            title = item.name,
                            subTitle = "#$index",
                            isSelected = { isSelected(item) },
                            songCount = item.songs.size.toLong(),
                            imageSource = { item.songs.firstOrNull() },
                            isPlaying = { item.songs.any { MPlayer.isItemPlaying(it.id) } },
                            onClick = {
                                if (isSelecting()) {
                                    onSelect(item)
                                } else {
                                    AppRouter.intent(NavIntent.Push(ArtistDetailScreen(item.id)))
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}