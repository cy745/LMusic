package com.lalilu.lmusic.compose.new_screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmusic.compose.component.SmartContainer
import com.lalilu.lmusic.compose.component.base.SongsSelectWrapper
import com.lalilu.lmusic.compose.component.card.SongCard
import com.lalilu.lmusic.compose.component.navigate.NavigatorHeader
import com.lalilu.lmusic.compose.new_screen.destinations.SongDetailScreenDestination
import com.lalilu.lmusic.viewmodel.HistoryViewModel
import com.lalilu.lmusic.viewmodel.PlayingViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import org.koin.androidx.compose.get

@OptIn(ExperimentalFoundationApi::class)
@HomeNavGraph
@Destination
@Composable
fun HistoryScreen(
    historyVM: HistoryViewModel = get(),
    playingVM: PlayingViewModel = get(),
    navigator: DestinationsNavigator
) {
    val haptic = LocalHapticFeedback.current
    val songs by historyVM.historyState

    SongsSelectWrapper { selector ->
        SmartContainer.LazyVerticalGrid(
            columns = { if (it == WindowWidthSizeClass.Expanded) 2 else 1 },
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            item {
                NavigatorHeader(
                    title = "播放历史",
                    subTitle = "共 ${songs.size} 首歌曲"
                ) {
                }
            }
            items(
                items = songs,
                key = { it.first.id },
                contentType = { LSong::class }
            ) { item ->
                val hasLyric = playingVM.requireHasLyricState(item.first)
                SongCard(
                    modifier = Modifier.animateItemPlacement(),
                    dragModifier = Modifier,
                    title = { item.first.name },
                    subTitle = { "${item.second}  ${item.first._artist}" },
                    mimeType = { item.first.mimeType },
                    duration = { item.first.durationMs },
                    hasLyric = { hasLyric.value },
                    imageData = { item.first },
                    onClick = {
                        if (selector.isSelecting.value) {
                            selector.onSelected(item.first)
                        } else {
                            historyVM.requiteHistoryList {
                                playingVM.playSongWithPlaylist(it, item.first)
                            }
                        }
                    },
                    onLongClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        navigator.navigate(SongDetailScreenDestination(mediaId = item.first.id))
                    },
                    onEnterSelect = { selector.onSelected(item.first) },
                    isSelected = { selector.selectedItems.any { it.id == item.first.id } }
                )
            }
        }
    }
}