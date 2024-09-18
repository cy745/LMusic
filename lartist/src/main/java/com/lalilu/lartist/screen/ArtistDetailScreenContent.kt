package com.lalilu.lartist.screen

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gigamole.composefadingedges.FadingEdgesGravity
import com.gigamole.composefadingedges.content.FadingEdgesContentType
import com.gigamole.composefadingedges.content.scrollconfig.FadingEdgesScrollConfig
import com.gigamole.composefadingedges.fill.FadingEdgesFillType
import com.gigamole.composefadingedges.verticalFadingEdges
import com.lalilu.common.base.Playable
import com.lalilu.component.base.songs.SongsScreenStickyHeader
import com.lalilu.component.card.SongCard
import com.lalilu.component.extension.rememberLazyListAnimateScroller
import com.lalilu.component.extension.startRecord
import com.lalilu.component.navigation.AppRouter
import com.lalilu.component.navigation.NavIntent
import com.lalilu.lartist.component.ArtistCard
import com.lalilu.lartist.viewModel.ArtistDetailSM
import com.lalilu.lmedia.entity.LArtist
import com.lalilu.lmedia.extension.GroupIdentity
import com.lalilu.lplayer.extensions.PlayerAction

@Composable
internal fun ArtistDetailScreenContent(
    artistDetailSM: ArtistDetailSM,
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
        keys = { artistDetailSM.recorder.list().filterNotNull() }
    )

    val artist by artistDetailSM.artist
    val songs by artistDetailSM.songs

    val relateArtist = remember(artist) {
        artist?.songs?.map { it.artists }
            ?.flatten()
            ?.toSet()
            ?.filter { it.id != artist!!.name }
            ?.toList()
            ?: emptyList()
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
        startRecord(artistDetailSM.recorder) {
            itemWithRecord(key = "HEADER") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .statusBarsPadding(),
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
                        imageSource = { item.songs.firstOrNull()?.imageSource },
                        isPlaying = {
                            false
//                        playingVM.isItemPlaying { playing ->
//                            playing.let { it as? LSong }
//                                ?.let { song -> song.artists.any { it.name == item.name } }
//                                ?: false
//                        }
                        },
                        onClick = { AppRouter.intent(NavIntent.Push(ArtistDetailScreen(item.id))) }
                    )
                }
            }
        }
    }
}