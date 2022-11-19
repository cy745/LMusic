package com.lalilu.lmusic.compose.screen.library

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lalilu.R
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmusic.compose.component.SmartContainer
import com.lalilu.lmusic.compose.component.card.RecommendCard
import com.lalilu.lmusic.compose.component.card.RecommendCard2
import com.lalilu.lmusic.compose.component.card.SongCard
import com.lalilu.lmusic.compose.screen.ScreenActions
import com.lalilu.lmusic.compose.screen.ScreenData
import com.lalilu.lmusic.utils.extension.dayNightTextColor
import com.lalilu.lmusic.utils.recomposeHighlighter
import com.lalilu.lmusic.viewmodel.LibraryViewModel
import com.lalilu.lmusic.viewmodel.PlayingViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel = hiltViewModel(),
    playingVM: PlayingViewModel = hiltViewModel()
) {
    val dailyRecommends = remember { viewModel.requireDailyRecommends() }
    val navToSongAction = ScreenActions.navToSongById(popUpToRoute = ScreenData.Library)
    val navToSongActionHaptic = ScreenActions.navToSongById(
        popUpToRoute = ScreenData.Library,
        hapticType = HapticFeedbackType.LongPress
    )

    val navToSongsAction = ScreenActions.navToSongs()
    val navToArtistsAction = ScreenActions.navToArtists()
    val navToAlbumsAction = ScreenActions.navToAlbums()
    val navToSettingsAction = ScreenActions.navToSettings()

    SmartContainer.LazyColumn {
        item {
            RecommendTitle(title = "每日推荐", onClick = { })
            RecommendRow(
                items = dailyRecommends,
                getId = { it.id }
            ) {
                RecommendCard2(
                    contentModifier = Modifier.size(width = 250.dp, height = 250.dp),
                    item = { it },
                    onClick = { navToSongAction(it.id) }
                )
            }
        }

        item {
            RecommendTitle(title = "最近添加", onClick = { })
            RecommendRow(
                items = viewModel.recentlyAdded.value,
                getId = { it.id }
            ) {
                RecommendCard(
                    modifier = Modifier.animateItemPlacement(),
                    width = { 100.dp },
                    height = { 100.dp },
                    item = { it },
                    onClick = { navToSongAction(it.id) },
                    onClickButton = { playingVM.playOrPauseSong(it.id) },
                    isPlaying = { playingVM.isSongPlaying(it.id) }
                )
            }
        }


        item {
            RecommendTitle(title = "最近播放")
        }
        items(
            items = viewModel.lastPlayedStack.value,
            key = { it.id },
            contentType = { LSong::class }
        ) {
            SongCard(
                modifier = Modifier.animateItemPlacement(),
                lyricRepository = playingVM.lyricRepository,
                song = { it },
                onLongClick = { navToSongActionHaptic(it.id) },
                onClick = { playingVM.browser.addAndPlay(it.id) }
            )
        }

        item {
            NavigationGrid(
                listOf(
                    ScreenData.Songs,
                    ScreenData.Albums,
                    ScreenData.Artists,
                    ScreenData.Settings
                )
            ) {
                when (it) {
                    ScreenData.Songs -> navToSongsAction()
                    ScreenData.Albums -> navToAlbumsAction()
                    ScreenData.Artists -> navToArtistsAction()
                    ScreenData.Settings -> navToSettingsAction()
                    else -> {}
                }
            }
        }


        item {
            RecommendTitle(title = "随机推荐", onClick = { }) {
                Surface(
                    shape = RoundedCornerShape(50.dp),
                    color = dayNightTextColor(0.05f),
                    onClick = { viewModel.refreshRandomRecommend() }
                ) {
                    Text(
                        modifier = Modifier.padding(horizontal = 15.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.subtitle2,
                        color = dayNightTextColor(0.7f),
                        text = "换一批"
                    )
                }
            }
        }
        item {
            RecommendRow(
                items = viewModel.randomRecommends.value,
                getId = { it.id }
            ) {
                RecommendCard2(
                    modifier = Modifier.animateItemPlacement(),
                    contentModifier = Modifier.size(width = 100.dp, height = 250.dp),
                    item = { it },
                    onClick = { navToSongAction(it.id) }
                )
            }
        }
    }
}

@Composable
fun RecommendTitle(
    modifier: Modifier = Modifier,
    title: String,
    onClick: () -> Unit = {},
    extraContent: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(15.dp)
            .recomposeHighlighter(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = title,
            style = MaterialTheme.typography.h6,
            color = dayNightTextColor()
        )
        extraContent()
    }
}

@Composable
fun RecommendTitle(modifier: Modifier = Modifier, title: String, onClick: () -> Unit = {}) {
    RecommendTitle(modifier = modifier, title = title, onClick = onClick) {
        Icon(
            painter = painterResource(id = R.drawable.ic_arrow_right_s_line),
            contentDescription = "",
            tint = dayNightTextColor()
        )
    }
}

@Composable
fun <I> RecommendRow(
    items: List<I>,
    getId: (I) -> Any,
    scrollToStartWhenUpdate: Boolean = false,
    itemContent: @Composable LazyItemScope.(item: I) -> Unit
) {
    val rowState = rememberLazyListState()

    if (scrollToStartWhenUpdate) {
        LaunchedEffect(items) {
            delay(50L)
            rowState.animateScrollToItem(0)
        }
    }

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = SpringSpec(stiffness = Spring.StiffnessLow)
            )
            .recomposeHighlighter(),
        state = rowState,
        horizontalArrangement = Arrangement.spacedBy(15.dp),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
    ) {
        items(items = items, key = getId) {
            itemContent(it)
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun NavigationGrid(
    items: List<ScreenData>,
    onClick: (ScreenData) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 15.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        items.forEach {
            Surface(
                color = dayNightTextColor(0.05f),
                shape = RoundedCornerShape(10.dp),
                onClick = { onClick(it) }
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 15.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
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