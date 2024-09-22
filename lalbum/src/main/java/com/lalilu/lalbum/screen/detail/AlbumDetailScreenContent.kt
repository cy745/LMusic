package com.lalilu.lalbum.screen.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import coil3.request.placeholder
import com.gigamole.composefadingedges.FadingEdgesGravity
import com.gigamole.composefadingedges.content.FadingEdgesContentType
import com.gigamole.composefadingedges.content.scrollconfig.FadingEdgesScrollConfig
import com.gigamole.composefadingedges.fill.FadingEdgesFillType
import com.gigamole.composefadingedges.verticalFadingEdges
import com.lalilu.common.base.Playable
import com.lalilu.component.R
import com.lalilu.component.base.smartBarPadding
import com.lalilu.component.base.songs.SongsScreenStickyHeader
import com.lalilu.component.card.SongCard
import com.lalilu.component.extension.rememberLazyListAnimateScroller
import com.lalilu.component.extension.startRecord
import com.lalilu.component.navigation.AppRouter
import com.lalilu.lalbum.viewModel.AlbumDetailSM
import com.lalilu.lalbum.viewModel.AlbumDetailScreenEvent
import com.lalilu.lmedia.extension.GroupIdentity
import com.lalilu.lplayer.extensions.PlayerAction
import kotlinx.coroutines.flow.collectLatest

@Composable
internal fun AlbumDetailScreenContent(
    albumDetailSM: AlbumDetailSM,
    isSelecting: () -> Boolean = { false },
    isSelected: (Playable) -> Boolean = { false },
    onSelect: (Playable) -> Unit = {},
    onClickGroup: (GroupIdentity) -> Unit = {}
) {
    val listState = rememberLazyListState()
    val statusBar = WindowInsets.statusBars
    val density = LocalDensity.current
    val stickyHeaderContentType = remember { "group" }
    val hapticFeedback = LocalHapticFeedback.current
    val scroller = rememberLazyListAnimateScroller(
        listState = listState,
        keys = { albumDetailSM.recorder.list().filterNotNull() }
    )

    LaunchedEffect(key1 = Unit) {
        albumDetailSM.eventFlow.collectLatest { event ->
            when (event) {
                is AlbumDetailScreenEvent.ScrollToItem -> {
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
            }
        }
    }

    val album by albumDetailSM.album
    val songs by albumDetailSM.songs

//    val relateArtist = remember(album) {
//        album?.songs?.map { it.artists }
//            ?.flatten()
//            ?.distinct()
//            ?: emptyList()
//    }

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
        startRecord(albumDetailSM.recorder) {
            itemWithRecord(key = "COVER") {
                val data = ImageRequest.Builder(LocalContext.current)
                    .data(album)
                    .placeholder(R.drawable.ic_music_2_line_100dp)
                    .error(R.drawable.ic_music_2_line_100dp)
                    .crossfade(true)
                    .build()

                Surface(
                    modifier = Modifier
                        .padding(16.dp)
                        .statusBarsPadding(),
                    elevation = 1.dp,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    AsyncImage(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f),
                        model = data,
                        contentDescription = null
                    )
                }
            }

            itemWithRecord(key = "HEADER") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = album?.name ?: "Unknown",
                        fontSize = 20.sp,
                        lineHeight = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colors.onBackground
                    )
                    Text(
                        text = "共 ${songs.size} 首歌曲",
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

//            if (relateArtist.isNotEmpty()) {
//                itemWithRecord(key = "EXTRA_HEADER") {
//                    Column(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(16.dp)
//                            .statusBarsPadding(),
//                        verticalArrangement = Arrangement.spacedBy(8.dp)
//                    ) {
//                        Text(
//                            text = "相关艺术家",
//                            fontSize = 20.sp,
//                            lineHeight = 20.sp,
//                            fontWeight = FontWeight.Bold,
//                            color = MaterialTheme.colors.onBackground
//                        )
//                    }
//                }
//
//                itemsIndexedWithRecord(
//                    items = relateArtist,
//                    key = { _, item -> item.id },
//                    contentType = { _, _ -> LArtist::class }
//                ) { index, item ->
//                    ArtistCard(
//                        modifier = Modifier.animateItem(),
//                        title = item.name,
//                        subTitle = "#$index",
//                        songCount = item.songs.size.toLong(),
//                        imageSource = { item.songs.firstOrNull()?.imageSource },
//                        isPlaying = {
//                            false
////                        playingVM.isItemPlaying { playing ->
////                            playing.let { it as? LSong }
////                                ?.let { song -> song.artists.any { it.name == item.name } }
////                                ?: false
////                        }
//                        },
//                        onClick = { AppRouter.intent(NavIntent.Push(ArtistDetailScreen(item.id))) }
//                    )
//                }
//            }
        }

        smartBarPadding()
    }
}