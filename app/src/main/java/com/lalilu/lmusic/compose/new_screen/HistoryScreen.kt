package com.lalilu.lmusic.compose.new_screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lalilu.R
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
            item(key = "header") {
                NavigatorHeader(
                    title = "播放历史",
                    subTitle = "共 ${songs.size} 首歌曲"
                )
            }
            items(
                items = songs,
                key = { it.id },
                contentType = { LSong::class }
            ) { item ->
                val hasLyric = playingVM.requireHasLyricState(item)
                SongCard(
                    modifier = Modifier.animateItemPlacement(),
                    dragModifier = Modifier,
                    title = { item.name },
                    subTitle = { item._artist },
                    mimeType = { item.mimeType },
                    duration = { item.durationMs },
                    hasLyric = { hasLyric.value },
                    imageData = { item },
                    isPlaying = { playingVM.isSongPlaying(item.id) },
                    onClick = {
                        if (selector.isSelecting.value) {
                            selector.onSelected(item)
                        } else {
                            historyVM.requiteHistoryList {
                                playingVM.playSongWithPlaylist(it, item)
                            }
                        }
                    },
                    onLongClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        navigator.navigate(SongDetailScreenDestination(mediaId = item.id))
                    },
                    onEnterSelect = { selector.onSelected(item) },
                    isSelected = { selector.selectedItems.any { it.id == item.id } },
                    showPrefix = { true },
                    prefixContent = { modifier ->
                        Row(
                            modifier = modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colors.surface)
                                .padding(start = 4.dp, end = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(
                                modifier = Modifier.size(10.dp),
                                painter = painterResource(id = R.drawable.headphone_fill),
                                contentDescription = ""
                            )
                            Text(
                                text = historyVM.requiteHistoryCountById(item.id).toString(),
                                fontSize = 12.sp
                            )
                        }
                    }
                )
            }
        }
    }
}