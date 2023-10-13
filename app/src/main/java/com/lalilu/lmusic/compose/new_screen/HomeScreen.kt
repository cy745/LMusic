package com.lalilu.lmusic.compose.new_screen

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Chip
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import com.lalilu.common.base.Playable
import com.lalilu.lmusic.utils.extension.singleViewModel
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmusic.compose.component.base.rememberSongsSelectWrapper
import com.lalilu.lmusic.compose.component.card.RecommendCard
import com.lalilu.lmusic.compose.component.card.RecommendRow
import com.lalilu.lmusic.compose.component.card.RecommendTitle
import com.lalilu.lmusic.compose.component.card.SongCard
import com.lalilu.lmusic.compose.new_screen.destinations.AlbumsScreenDestination
import com.lalilu.lmusic.compose.new_screen.destinations.ArtistsScreenDestination
import com.lalilu.lmusic.compose.new_screen.destinations.DictionariesScreenDestination
import com.lalilu.lmusic.compose.new_screen.destinations.ExtensionsScreenDestination
import com.lalilu.lmusic.compose.new_screen.destinations.HistoryScreenDestination
import com.lalilu.lmusic.compose.new_screen.destinations.SettingsScreenDestination
import com.lalilu.lmusic.compose.new_screen.destinations.SongDetailScreenDestination
import com.lalilu.lmusic.compose.new_screen.destinations.SongsScreenDestination
import com.lalilu.lmusic.utils.extension.dayNightTextColor
import com.lalilu.lmusic.viewmodel.ExtensionsViewModel
import com.lalilu.lmusic.viewmodel.HistoryViewModel
import com.lalilu.lmusic.viewmodel.LibraryViewModel
import com.lalilu.lmusic.viewmodel.PlayingViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@HomeNavGraph(start = true)
@Destination
@Composable
fun HomeScreen(
    vm: LibraryViewModel = singleViewModel(),
    historyVM: HistoryViewModel = singleViewModel(),
    playingVM: PlayingViewModel = singleViewModel(),
    extensionsVM: ExtensionsViewModel = singleViewModel(),
    navigator: DestinationsNavigator,
) {
    val extensionResult by extensionsVM.extensionWithHomeContent

    val haptic = LocalHapticFeedback.current
    val selectHelper = rememberSongsSelectWrapper()
    val itemsCount = remember {
        derivedStateOf { historyVM.historyState.value.size.coerceIn(0, 5) }
    }
    val itemsHeight = animateDpAsState(
        itemsCount.value * 85.dp
    )

    LaunchedEffect(Unit) {
        vm.checkOrUpdateToday()
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(items = extensionResult) { it.apply { Place(contentKey = "home") } }

        item {
            RecommendTitle(
                title = "最近添加",
                onClick = { }
            ) {
                Chip(
                    onClick = { navigator.navigate(SongsScreenDestination()) },
                ) {
                    Text(
                        style = MaterialTheme.typography.caption,
                        text = "所有歌曲"
                    )
                }
            }
        }
        item {
            RecommendRow(
                items = { vm.recentlyAdded.value },
                getId = { it.id }
            ) {
                RecommendCard(
                    item = { it },
                    width = { 100.dp },
                    height = { 100.dp },
                    modifier = Modifier.animateItemPlacement(),
                    onClick = { navigator.navigate(SongDetailScreenDestination(it.id)) },
                    onClickButton = {
                        playingVM.play(
                            mediaId = it.id,
                            playOrPause = true,
                            addToNext = true
                        )
                    },
                    isPlaying = { playingVM.isItemPlaying(it.id, Playable::mediaId) }
                )
            }
        }

        item {
            RecommendTitle(
                title = "最近播放",
                onClick = { }
            ) {
                Chip(
                    onClick = { navigator.navigate(HistoryScreenDestination) },
                ) {
                    Text(
                        style = MaterialTheme.typography.caption,
                        text = "历史记录"
                    )
                }
            }
        }

        item {
            LazyColumn(
                modifier = Modifier
                    .height(itemsHeight.value)
                    .animateContentSize()
                    .fillMaxWidth()
            ) {
                items(
                    items = historyVM.historyState.value.take(5),
                    key = { it.id },
                    contentType = { LSong::class }
                ) { item ->
                    SongCard(
                        song = { item },
                        modifier = Modifier
                            .animateItemPlacement()
                            .padding(bottom = 5.dp),
                        fixedHeight = { true },
                        isSelected = { selectHelper.selectedItems.any { it.id == item.id } },
                        hasLyric = playingVM.lyricRepository.rememberHasLyric(playable = item),
                        onEnterSelect = { selectHelper.onSelected(item) },
                        isPlaying = { playingVM.isItemPlaying(item.id, Playable::mediaId) },
                        onClick = {
                            if (selectHelper.isSelecting.value) {
                                selectHelper.onSelected(item)
                            } else {
                                historyVM.requiteHistoryList {
                                    playingVM.play(
                                        mediaId = item.mediaId,
                                        mediaIds = it.map(Playable::mediaId),
                                        playOrPause = true
                                    )
                                }
                            }
                        },
                        onLongClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            navigator.navigate(SongDetailScreenDestination(item.id))
                        }
                    )
                }
            }
        }

        item {
            Surface(
                modifier = Modifier.padding(15.dp),
                shape = RoundedCornerShape(15.dp)
            ) {
                Column {
                    listOf(
                        ScreenData.Songs,
                        ScreenData.Albums,
                        ScreenData.Artists,
                        ScreenData.Dictionaries,
                        ScreenData.Extensions,
                        ScreenData.Settings
                    ).forEach {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    when (it) {
                                        ScreenData.Songs -> navigator.navigate(
                                            SongsScreenDestination()
                                        )

                                        ScreenData.Albums -> navigator.navigate(
                                            AlbumsScreenDestination()
                                        )

                                        ScreenData.Artists -> navigator.navigate(
                                            ArtistsScreenDestination()
                                        )

                                        ScreenData.Extensions -> navigator.navigate(
                                            ExtensionsScreenDestination
                                        )

                                        ScreenData.Settings -> navigator.navigate(
                                            SettingsScreenDestination
                                        )

                                        ScreenData.Dictionaries -> navigator.navigate(
                                            DictionariesScreenDestination
                                        )

                                        else -> {}
                                    }
                                }
                                .padding(horizontal = 20.dp, vertical = 15.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = it.icon),
                                contentDescription = stringResource(id = it.title),
                                tint = dayNightTextColor(0.7f)
                            )
                            Text(
                                text = stringResource(id = it.title),
                                color = dayNightTextColor(0.6f),
                                style = MaterialTheme.typography.subtitle2
                            )
                        }
                    }
                }
            }
        }
    }
}
