package com.lalilu.lmusic.screen.library

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import com.lalilu.lmusic.screen.ScreenActions
import com.lalilu.lmusic.screen.component.SmartContainer
import com.lalilu.lmusic.screen.component.card.SongCard
import com.lalilu.lmusic.service.runtime.LMusicRuntime
import com.lalilu.lmusic.utils.extension.LocalWindowSize
import com.lalilu.lmusic.utils.extension.average
import com.lalilu.lmusic.viewmodel.LibraryViewModel
import com.lalilu.lmusic.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@Composable
fun SongsScreen(
    mainViewModel: MainViewModel,
    libraryViewModel: LibraryViewModel,
) {
    val songs by libraryViewModel.songs.observeAsState(emptyList())
    val currentPlaying by LMusicRuntime.playingLiveData.observeAsState()

    val windowSize = LocalWindowSize.current
    val gridState = rememberLazyGridState()
    val scope = rememberCoroutineScope()

    val navToSongAction = ScreenActions.navToSong(hapticType = HapticFeedbackType.LongPress)

    val onSongSelected: (Int) -> Unit = remember(songs) {
        { index ->
            mainViewModel.playSongWithPlaylist(
                items = songs.toMutableList(),
                index = index
            )
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

    SmartContainer.LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Fixed(if (windowSize.widthSizeClass == WindowWidthSizeClass.Expanded) 2 else 1)
    ) {
        songs.forEachIndexed { index, item ->
            item(key = item.id, contentType = item::class) {
                SongCard(
                    index = index,
                    getSong = { item },
                    loadDelay = { 200L },
                    onSongSelected = onSongSelected,
                    onSongShowDetail = navToSongAction
                )
            }
        }
    }
}