package com.lalilu.lmusic.compose.screen.library

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lalilu.R
import com.lalilu.lmusic.compose.component.SmartBar
import com.lalilu.lmusic.compose.component.SmartContainer
import com.lalilu.lmusic.compose.component.card.RecommendCard
import com.lalilu.lmusic.compose.component.card.RecommendCard2
import com.lalilu.lmusic.compose.screen.ScreenActions
import com.lalilu.lmusic.compose.screen.ScreenData
import com.lalilu.lmusic.utils.extension.dayNightTextColor
import com.lalilu.lmusic.utils.recomposeHighlighter
import com.lalilu.lmusic.viewmodel.LibraryViewModel
import com.lalilu.lmusic.viewmodel.PlayingViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel = hiltViewModel(),
    playingVM: PlayingViewModel = hiltViewModel()
) {
    val dailyRecommends = remember { viewModel.requireDailyRecommends() }
    val lastPlayedStack by viewModel.lastPlayedStack.observeAsState(emptyList())
    val recentlyAdded by viewModel.recentlyAdded.observeAsState(emptyList())
    val randomRecommends by viewModel.randomRecommendsLiveData.observeAsState(emptyList())

    val currentPlaying by playingVM.runtime.playingLiveData.observeAsState()
    val currentIsPlaying by playingVM.runtime.isPlayingLiveData.observeAsState(false)

    val navToSongAction = ScreenActions.navToSongFromLibrary()

    val playSong = remember {
        { mediaId: String ->
            currentPlaying.takeIf { it != null && it.id == mediaId && currentIsPlaying }
                ?.let { playingVM.browser.pause() } ?: playingVM.browser.addAndPlay(mediaId)
        }
    }

    val isChecked = ScreenData.Library.isChecked?.value
    LaunchedEffect(isChecked) {
        if (isChecked == true) {
            SmartBar.setExtraBar {
                SearchInputBar(value = "", onSearchFor = {}, onChecked = {})
            }
        } else {
            SmartBar.setExtraBar(item = null)
        }
    }

    SmartContainer.LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            RecommendTitle("每日推荐", onClick = { })
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
            RecommendTitle("最近添加", onClick = { })
            RecommendRow(
                items = recentlyAdded.toList(),
                getId = { it.id }
            ) {
                RecommendCard(
                    modifier = Modifier.animateItemPlacement(),
                    width = { 200.dp },
                    height = { 125.dp },
                    item = { it },
                    isPlaying = { currentIsPlaying && currentPlaying != null && it.id == currentPlaying?.id },
                    onClick = { navToSongAction(it.id) },
                    onClickButton = { playSong(it.id) }
                )
            }
        }


        item {
            RecommendTitle("最近播放")
            RecommendRow(
                scrollToStartWhenUpdate = true,
                items = lastPlayedStack.toList(),
                getId = { it.id }
            ) {
                RecommendCard(
                    modifier = Modifier.animateItemPlacement(),
                    width = { 125.dp },
                    height = { 125.dp },
                    item = { it },
                    isPlaying = { currentIsPlaying && currentPlaying != null && it.id == currentPlaying?.id },
                    onClick = { navToSongAction(it.id) },
                    onClickButton = { playSong(it.id) }
                )
            }
        }

        item {
            RecommendTitle("随机推荐", onClick = { viewModel.refreshRandomRecommend() })
            RecommendRow(
                items = randomRecommends.toList(),
                getId = { it.id }
            ) {
                RecommendCard2(
                    modifier = Modifier.animateItemPlacement(),
                    contentModifier = Modifier.size(width = 125.dp, height = 250.dp),
                    item = { it },
                    onClick = { navToSongAction(it.id) }
                )
            }
        }
    }
}

@Composable
fun RecommendTitle(
    title: String,
    onClick: () -> Unit = {},
    extraContent: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = Modifier
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
fun RecommendTitle(title: String, onClick: () -> Unit = {}) {
    RecommendTitle(title = title, onClick = onClick) {
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