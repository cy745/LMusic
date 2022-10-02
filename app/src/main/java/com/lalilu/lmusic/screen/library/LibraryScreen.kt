package com.lalilu.lmusic.screen.library

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.lalilu.R
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmedia.indexer.Library
import com.lalilu.lmusic.screen.ScreenData
import com.lalilu.lmusic.screen.component.SmartBar
import com.lalilu.lmusic.screen.component.SmartContainer
import com.lalilu.lmusic.screen.component.card.ExpendableTextCard
import com.lalilu.lmusic.screen.component.card.RecommendCard
import com.lalilu.lmusic.screen.component.card.RecommendCard2
import com.lalilu.lmusic.service.LMusicBrowser
import com.lalilu.lmusic.service.LMusicRuntime
import com.lalilu.lmusic.utils.extension.LocalNavigatorHost
import com.lalilu.lmusic.utils.extension.dayNightTextColor
import com.lalilu.lmusic.utils.recomposeHighlighter
import com.lalilu.lmusic.viewmodel.LibraryViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel
) {
    val navController = LocalNavigatorHost.current
    val dailyRecommends = remember { viewModel.requireDailyRecommends() }
    val lastPlayedStack by viewModel.lastPlayedStack.observeAsState(emptyList())
    val recentlyAdded by viewModel.recentlyAdded.observeAsState(emptyList())
    val randomRecommends = remember { Library.getSongs(10, true) }

    val currentPlaying by LMusicRuntime.currentPlayingLiveData.observeAsState()
    val currentIsPlaying by LMusicRuntime.currentIsPlayingLiveData.observeAsState(false)

    val playSong = remember {
        { mediaId: String ->
            currentPlaying.takeIf { it != null && it.id == mediaId && currentIsPlaying }
                ?.let { LMusicBrowser.pause() } ?: LMusicBrowser.addAndPlay(mediaId)
        }
    }

    val showDetail = remember {
        { mediaId: String ->
            navController.navigate("${ScreenData.SongsDetail.name}/$mediaId") {
                popUpTo(navController.graph.findStartDestination().id) {
                    //出栈的 BackStack 保存状态
                    saveState = true
                }
                // 避免点击同一个 Item 时反复入栈
                launchSingleTop = true

                // 如果之前出栈时保存状态了，那么重新入栈时恢复状态
                restoreState = true
            }
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
            RecommendRow(items = dailyRecommends, getId = { it.id }) {
                RecommendCard2(
                    data = { it },
                    getId = { it.id },
                    width = 250.dp,
                    height = 250.dp,
                    onShowDetail = showDetail
                ) {
                    ExpendableTextCard(
                        title = it.name,
                        subTitle = it._artist,
                        defaultState = true,
                        titleColor = Color.White,
                        subTitleColor = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }

        item {
            // 最近添加
            RecommendTitle("最近添加", onClick = { })
            RecommendRow(items = recentlyAdded.toList(), getId = { it.id }) {
                RecommendCardWithOutSideText(
                    modifier = Modifier.animateItemPlacement(),
                    getSong = { it },
                    isPlaying = (currentIsPlaying && currentPlaying != null && it.id == currentPlaying?.id),
                    onPlaySong = playSong,
                    onShowDetail = showDetail
                )
            }
        }


        item {
            RecommendTitle("最近播放")
            RecommendRow(
                scrollToStartWhenUpdate = true,
                items = lastPlayedStack.toList(),
                getId = { it.id }) {
                RecommendCardWithOutSideText(
                    modifier = Modifier.animateItemPlacement(),
                    getSong = { it },
                    width = 125.dp,
                    height = 125.dp,
                    isPlaying = (currentIsPlaying && currentPlaying != null && it.id == currentPlaying?.id),
                    onPlaySong = playSong,
                    onShowDetail = showDetail
                )
            }
        }

        item {
            RecommendTitle("随机推荐", onClick = { })
            RecommendRow(items = randomRecommends.toList(), getId = { it.id }) {
                RecommendCard2(
                    modifier = Modifier.animateItemPlacement(),
                    data = { it },
                    getId = { it.id },
                    width = 125.dp,
                    height = 250.dp,
                    onShowDetail = showDetail
                ) {
                    ExpendableTextCard(
                        title = it.name,
                        subTitle = it._artist,
                        titleColor = Color.White,
                        subTitleColor = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
fun RecommendTitle(title: String, onClick: () -> Unit = {}) {
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

@Composable
fun RecommendCardWithOutSideText(
    modifier: Modifier = Modifier,
    getSong: () -> LSong,
    width: Dp = 200.dp,
    height: Dp = 125.dp,
    isPlaying: Boolean = false,
    onShowDetail: (String) -> Unit = {},
    onPlaySong: (String) -> Unit = {}
) {
    val song = getSong()

    Column(
        modifier = modifier
            .width(IntrinsicSize.Min)
            .recomposeHighlighter(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        RecommendCard(
            data = { song },
            getId = { song.id },
            isPlaying = isPlaying,
            width = width,
            height = height,
            onPlay = onPlaySong,
            onShowDetail = onShowDetail
        )
        ExpendableTextCard(
            title = song.name, subTitle = song._artist
        )
    }
}