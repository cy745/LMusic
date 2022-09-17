package com.lalilu.lmusic.screen.library

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lalilu.R
import com.lalilu.lmedia.indexer.Library
import com.lalilu.lmusic.screen.MainScreenData
import com.lalilu.lmusic.screen.component.SmartBar
import com.lalilu.lmusic.screen.component.card.SongCard
import com.lalilu.lmusic.service.LMusicRuntime
import com.lalilu.lmusic.utils.extension.LocalNavigatorHost
import com.lalilu.lmusic.utils.extension.LocalWindowSize
import com.lalilu.lmusic.utils.extension.average
import com.lalilu.lmusic.utils.extension.dayNightTextColor
import com.lalilu.lmusic.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@Composable
fun SongsScreen(
    mainViewModel: MainViewModel = hiltViewModel(),
) {
    val songs = Library.getSongs()
    val windowSize = LocalWindowSize.current
    val haptic = LocalHapticFeedback.current
    val navController = LocalNavigatorHost.current
    val currentPlaying by LMusicRuntime.currentPlayingFlow.collectAsState()
    val gridState = rememberLazyGridState()
    val scope = rememberCoroutineScope()

    val onSongSelected: (Int) -> Unit = remember(songs) {
        { index ->
            mainViewModel.playSongWithPlaylist(
                items = songs.toMutableList(),
                index = index
            )
        }
    }
    val onSongShowDetail: (String) -> Unit = remember {
        { mediaId ->
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            navController.navigate("${MainScreenData.SongsDetail.name}/$mediaId")
        }
    }

    val scrollToCurrentPlaying = remember {
        {
            scope.launch {
                if (currentPlaying == null) return@launch

                val index = songs.indexOfFirst { it.id == currentPlaying!!.id }
                if (index >= 0) {
//                    gridState.animateScrollToItem(index)

                    // 获取当前可见元素的平均高度
                    fun getHeightAverage() = gridState.layoutInfo.visibleItemsInfo
                        .average { it.size.height }

                    // 获取精确的位移量（只能对可见元素获取）
                    fun getTargetOffset() = gridState.layoutInfo.visibleItemsInfo
                        .find { it.index == index }
                        ?.offset?.y

                    // 获取粗略的位移量（通过对可见元素的高度求平均再通过index的差，计算出粗略值）
                    fun getRoughTargetOffset() =
                        getHeightAverage() * (index - gridState.firstVisibleItemIndex - 1)

                    // 若获取不到精确的位移量，则计算粗略位移量并开始scroll
                    if (getTargetOffset() == null) {
                        gridState.animateScrollBy(
                            getRoughTargetOffset(),
                            SpringSpec(stiffness = Spring.StiffnessVeryLow)
                        )
                    }

                    // 若可以获取到精确的位移量，则直接滚动到目标歌曲位置
                    getTargetOffset()?.let {
                        gridState.animateScrollBy(
                            it.toFloat(),
                            SpringSpec(stiffness = Spring.StiffnessVeryLow)
                        )
                    }
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            modifier = Modifier.fillMaxSize(),
            state = gridState,
            columns = GridCells.Fixed(if (windowSize.widthSizeClass == WindowWidthSizeClass.Expanded) 2 else 1),
            contentPadding = SmartBar.rememberContentPadding()
        ) {
            songs.forEachIndexed { index, item ->
                item(key = item.id, contentType = item::class) {
                    SongCard(
                        index = index,
                        getSong = { item },
                        onSongSelected = onSongSelected,
                        onSongShowDetail = onSongShowDetail
                    )
                }
            }
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(SmartBar.rememberContentPadding(horizontal = 20.dp)),
            shape = CircleShape,
            elevation = 5.dp,
            color = Color.Gray.copy(alpha = 0.8f)
        ) {
            IconButton(onClick = { scrollToCurrentPlaying() }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_focus_3_line),
                    contentDescription = "",
                    tint = dayNightTextColor()
                )
            }
        }
    }
}