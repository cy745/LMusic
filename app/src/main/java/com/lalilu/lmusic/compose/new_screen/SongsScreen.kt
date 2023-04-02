package com.lalilu.lmusic.compose.new_screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.blankj.utilcode.util.TimeUtils
import com.lalilu.R
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmedia.extension.GroupRule
import com.lalilu.lmedia.extension.OrderRule
import com.lalilu.lmedia.extension.SortRule
import com.lalilu.lmedia.extension.Sortable
import com.lalilu.lmusic.compose.component.SmartBar
import com.lalilu.lmusic.compose.component.SmartContainer
import com.lalilu.lmusic.compose.component.SmartFloatBtns
import com.lalilu.lmusic.compose.component.SmartModalBottomSheet
import com.lalilu.lmusic.compose.component.base.SongsSelectWrapper
import com.lalilu.lmusic.compose.component.base.SortPanel
import com.lalilu.lmusic.compose.component.card.SongCard
import com.lalilu.lmusic.compose.component.navigate.NavigatorHeader
import com.lalilu.lmusic.compose.new_screen.destinations.SongDetailScreenDestination
import com.lalilu.lmusic.utils.extension.dayNightTextColor
import com.lalilu.lmusic.utils.extension.getIds
import com.lalilu.lmusic.viewmodel.PlayingViewModel
import com.lalilu.lmusic.viewmodel.SongsViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.launch
import org.koin.androidx.compose.get

@OptIn(ExperimentalMaterialApi::class)
@HomeNavGraph
@Destination
@Composable
fun SongsScreen(
    title: String = "全部歌曲",
    sortFor: String = Sortable.SORT_FOR_SONGS,
    mediaIdsText: String? = null,
    songsVM: SongsViewModel = get(),
    playingVM: PlayingViewModel = get(),
    navigator: DestinationsNavigator
) {
    val scope = rememberCoroutineScope()
    val gridState = rememberLazyGridState()
    val songsState by songsVM.songsState
    val currentPlaying by playingVM.currentPlaying.observeAsState()
    val showSortPanel = remember { mutableStateOf(false) }
    LaunchedEffect(mediaIdsText) {
        songsVM.updateByIds(
            songIds = mediaIdsText.getIds(),
            sortFor = sortFor
        )
    }

    SmartFloatBtns.RegisterFloatBtns(
        items = listOf(
            SmartFloatBtns.FloatBtnItem(
                title = "排序",
                icon = R.drawable.ic_sort_desc,
                callback = { showSortPanel.value = true }
            ),
            SmartFloatBtns.FloatBtnItem(
                icon = R.drawable.ic_focus_3_line,
                title = "定位当前播放歌曲",
                callback = {
                    if (currentPlaying != null) {
                        scope.launch {
                            var index = 0
                            songsState.forEach { entry ->
                                val tempIndex =
                                    entry.value.indexOfFirst { it.id == currentPlaying!!.id }
                                if (tempIndex != -1) {
                                    index += tempIndex
                                    gridState.animateScrollToItem(index)
                                }
                                index += entry.value.size
                            }
                        }
                    }
                }
            ),
            SmartFloatBtns.FloatBtnItem(
                icon = R.drawable.ic_arrow_up_s_line,
                title = "回到顶部",
                callback = { scope.launch { gridState.animateScrollToItem(0) } }
            ),
            SmartFloatBtns.FloatBtnItem(
                icon = R.drawable.ic_arrow_down_s_line,
                title = "滚动到底部",
                callback = { scope.launch { gridState.scrollToItem(Int.MAX_VALUE) } }
            )
        )
    )

    SortPanelWrapper(
        sortFor = sortFor,
        showPanelState = showSortPanel,
        supportGroupRules = { songsVM.sorter.supportGroupRules },
        supportOrderRules = { songsVM.sorter.supportOrderRules },
        supportSortRules = { songsVM.sorter.supportSortRules }
    ) {
        SongListWrapper(
            state = gridState,
            songsState = songsState,
            hasLyricState = { playingVM.requireHasLyricState(item = it) },
            isItemPlaying = { playingVM.isSongPlaying(mediaId = it.id) },
            onLongClickItem = { navigator.navigate(SongDetailScreenDestination(mediaId = it.id)) },
            onClickItem = { playingVM.playSongWithPlaylist(songsState.values.flatten(), it) }
        ) {
            item {
                NavigatorHeader(
                    title = title,
                    subTitle = "共 ${songsState.values.flatten().size} 首歌曲"
                ) {
                    Surface(
                        shape = RoundedCornerShape(50.dp),
                        color = dayNightTextColor(0.05f),
                        onClick = { showSortPanel.value = !showSortPanel.value }
                    ) {
                        Text(
                            modifier = Modifier.padding(horizontal = 15.dp, vertical = 5.dp),
                            style = MaterialTheme.typography.subtitle2,
                            color = dayNightTextColor(0.7f),
                            text = "排序"
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SortPanelWrapper(
    sortFor: String,
    showPanelState: MutableState<Boolean> = remember { mutableStateOf(false) },
    supportGroupRules: () -> List<GroupRule>,
    supportSortRules: () -> List<SortRule>,
    supportOrderRules: () -> List<OrderRule>,
    content: @Composable () -> Unit
) {
    SmartBar.RegisterMainBarContent(
        showState = showPanelState,
        showMask = true,
        showBackground = false
    ) {
        SortPanel(
            sortFor = sortFor,
            supportGroupRules = supportGroupRules,
            supportOrderRules = supportOrderRules,
            supportSortRules = supportSortRules,
            onClose = { showPanelState.value = false }
        )
        BackHandler(showPanelState.value && SmartModalBottomSheet.isVisible.value) {
            showPanelState.value = false
        }
    }
    content()
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SongListWrapper(
    state: LazyGridState = rememberLazyGridState(),
    songsState: Map<Any, List<LSong>>,
    hasLyricState: (LSong) -> State<Boolean>,
    onClickItem: (LSong) -> Unit = {},
    onLongClickItem: (LSong) -> Unit = {},
    isItemPlaying: (LSong) -> Boolean = { false },
    headerContent: LazyGridScope.() -> Unit
) {
    val haptic = LocalHapticFeedback.current

    SongsSelectWrapper { selector ->
        SmartContainer.LazyVerticalGrid(
            state = state,
            columns = { if (it == WindowWidthSizeClass.Expanded) 2 else 1 },
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            headerContent()

            val now = System.currentTimeMillis()
            songsState.forEach { (titleObj, list) ->
                if (titleObj is Long) {
                    item(
                        key = titleObj,
                        contentType = LSong::dateAdded,
                        span = { GridItemSpan(maxLineSpan) }
                    ) {
                        Text(
                            modifier = Modifier.padding(
                                top = 20.dp, bottom = 10.dp, start = 20.dp, end = 20.dp
                            ), style = MaterialTheme.typography.h6, text = when {
                                now - titleObj < 300000 -> "刚刚"
                                now - titleObj < 3600000 -> "${(now - titleObj) / 60000}分钟前"
                                now - titleObj < 86400000 -> "${(now - titleObj) / 3600000}小时前"
                                else -> TimeUtils.millis2String(titleObj, "M月d日 HH:mm")
                            }
                        )
                    }
                } else if (titleObj is String && titleObj.isNotEmpty()) {
                    item(
                        key = titleObj,
                        contentType = LSong::dateAdded,
                        span = { GridItemSpan(maxLineSpan) }
                    ) {
                        Text(
                            modifier = Modifier.padding(
                                top = 20.dp, bottom = 10.dp, start = 20.dp, end = 20.dp
                            ), style = MaterialTheme.typography.h6, text = titleObj
                        )
                    }
                }

                items(
                    items = list,
                    key = { it.id },
                    contentType = { LSong::class }
                ) { item ->
                    SongCard(
                        modifier = Modifier.animateItemPlacement(),
                        song = { item },
                        hasLyric = hasLyricState(item),
                        onClick = {
                            if (selector.isSelecting.value) {
                                selector.onSelected(item)
                            } else {
                                onClickItem(item)
                            }
                        },
                        onLongClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onLongClickItem(item)
                        },
                        onEnterSelect = { selector.onSelected(item) },
                        isSelected = { selector.selectedItems.any { it.id == item.id } },
                        isPlaying = { isItemPlaying(item) }
                    )
                }
            }
        }
    }
}