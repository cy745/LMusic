package com.lalilu.lmusic.compose.new_screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmusic.compose.component.SmartContainer
import com.lalilu.lmusic.compose.component.card.RecommendCard
import com.lalilu.lmusic.compose.component.card.RecommendCard2
import com.lalilu.lmusic.compose.component.card.SongCard
import com.lalilu.lmusic.compose.new_screen.destinations.AlbumsScreenDestination
import com.lalilu.lmusic.compose.new_screen.destinations.ArtistsScreenDestination
import com.lalilu.lmusic.compose.new_screen.destinations.DictionariesScreenDestination
import com.lalilu.lmusic.compose.new_screen.destinations.SettingsScreenDestination
import com.lalilu.lmusic.compose.new_screen.destinations.SongDetailScreenDestination
import com.lalilu.lmusic.compose.new_screen.destinations.SongsScreenDestination
import com.lalilu.lmusic.compose.screen.ScreenData
import com.lalilu.lmusic.compose.screen.library.RecommendRow
import com.lalilu.lmusic.compose.screen.library.RecommendTitle
import com.lalilu.lmusic.utils.extension.dayNightTextColor
import com.lalilu.lmusic.viewmodel.LibraryViewModel
import com.lalilu.lmusic.viewmodel.PlayingViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import org.koin.androidx.compose.get
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalFoundationApi::class)
@HomeNavGraph(start = true)
@Destination
@Composable
fun HomeScreen(
    vm: LibraryViewModel = koinViewModel(),
    playingVM: PlayingViewModel = get(),
    navigator: DestinationsNavigator
) {
    val dailyRecommends = remember { vm.requireDailyRecommends() }

    SmartContainer.LazyColumn {
        item {
            RecommendTitle(
                title = "每日推荐",
                onClick = {
                }
            )
        }
        item {
            RecommendRow(
                items = dailyRecommends,
                getId = { it.id }
            ) {
                RecommendCard2(
                    contentModifier = Modifier.size(width = 250.dp, height = 250.dp),
                    item = { it },
                    onClick = { navigator.navigate(SongDetailScreenDestination(it.id)) }
                )
            }
        }

        item {
            RecommendTitle(
                title = "最近添加",
                onClick = {
                }
            )
        }
        item {
            RecommendRow(
                items = vm.recentlyAdded.value,
                getId = { it.id }
            ) {
                RecommendCard(
                    modifier = Modifier.animateItemPlacement(),
                    width = { 100.dp },
                    height = { 100.dp },
                    item = { it },
                    onClick = { navigator.navigate(SongDetailScreenDestination(it.id)) },
                    onClickButton = {},
                    isPlaying = { false }
                )
            }
        }

        item {
            RecommendTitle(
                title = "最近播放",
                onClick = {
                }
            )
        }
        items(
            items = vm.lastPlayedStack.value,
            key = { it.id },
            contentType = { LSong::class }
        ) {
            SongCard(
                modifier = Modifier.animateItemPlacement(),
                hasLyric = playingVM.lyricRepository.rememberHasLyric(song = it),
                song = { it },
                onLongClick = { navigator.navigate(SongDetailScreenDestination(it.id)) },
                onClick = { }
            )
        }

        items(
            items = listOf(
                ScreenData.Songs,
                ScreenData.Albums,
                ScreenData.Artists,
                ScreenData.Dictionaries,
                ScreenData.Settings
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        when (it) {
                            ScreenData.Songs -> navigator.navigate(SongsScreenDestination)
                            ScreenData.Albums -> navigator.navigate(AlbumsScreenDestination)
                            ScreenData.Artists -> navigator.navigate(ArtistsScreenDestination)
                            ScreenData.Settings -> navigator.navigate(SettingsScreenDestination)
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
