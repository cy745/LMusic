package com.lalilu.lmusic.compose.screen.library

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.unit.dp
import com.blankj.utilcode.util.TimeUtils
import com.funny.data_saver.core.rememberDataSaverState
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmedia.extension.GroupRule
import com.lalilu.lmedia.extension.OrderRule
import com.lalilu.lmedia.extension.SortRule
import com.lalilu.lmedia.extension.Sortable
import com.lalilu.lmusic.compose.component.SmartBar
import com.lalilu.lmusic.compose.component.SmartContainer
import com.lalilu.lmusic.compose.component.base.IconTextButton
import com.lalilu.lmusic.compose.component.card.SongCard
import com.lalilu.lmusic.compose.screen.LibraryDetailNavigateBar
import com.lalilu.lmusic.compose.screen.ScreenActions
import com.lalilu.lmusic.utils.extension.LocalWindowSize
import com.lalilu.lmusic.utils.extension.buildScrollToItemAction
import com.lalilu.lmusic.utils.extension.dayNightTextColor
import com.lalilu.lmusic.utils.rememberSelectState
import com.lalilu.lmusic.viewmodel.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SongsScreen(
    mainVM: MainViewModel = LocalMainVM.current,
    libraryVM: LibraryViewModel = LocalLibraryVM.current,
    playingVM: PlayingViewModel = LocalPlayingVM.current
) {
    val windowSize = LocalWindowSize.current
    val gridState = rememberLazyGridState()

    val navToSongAction = ScreenActions.navToSongById(hapticType = HapticFeedbackType.LongPress)
    val navToAddToPlaylist = mainVM.navToAddToPlaylist()

    val songs by libraryVM.songs
    val currentPlaying by playingVM.runtime.playingLiveData.observeAsState()

    val scrollAction = buildScrollToItemAction(
        target = currentPlaying,
        getIndex = { songs.values.flatten().indexOfFirst { it.id == currentPlaying!!.id } },
        state = gridState
    )

    val selectedItems = remember { mutableStateListOf<LSong>() }
    val selector = rememberSelectState(
        defaultState = false, selectedItems = selectedItems
    )

    LaunchedEffect(selector.isSelecting.value) {
        if (selector.isSelecting.value) {
            SmartBar.setMainBar {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    IconTextButton(text = "取消",
                        color = Color(0xFF006E7C),
                        onClick = { selector.clear() })
                    Text(text = "已选择: ${selectedItems.size}")
                    IconTextButton(text = "添加到歌单",
                        color = Color(0xFF006E7C),
                        onClick = { navToAddToPlaylist(selectedItems) })
                }
            }
        } else {
            SmartBar.setMainBar(item = LibraryDetailNavigateBar)
        }
    }

    SmartContainer.LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Fixed(if (windowSize.widthSizeClass == WindowWidthSizeClass.Expanded) 2 else 1),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        val now = System.currentTimeMillis()

        item(key = "CONTROLLER", contentType = "CONTROLLER") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Button(onClick = {
                    SmartBar.setExtraBar {
                        SortBar(libraryVM = libraryVM, sortFor = Sortable.SORT_FOR_SONGS)
                    }
                }) {
                    Text(text = "选择排序")
                }
            }
        }

        songs.forEach { (titleObj, list) ->
            if (titleObj is Long) {
                item(key = titleObj,
                    contentType = LSong::dateAdded,
                    span = { GridItemSpan(maxLineSpan) }) {
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
                item(key = titleObj,
                    contentType = LSong::dateAdded,
                    span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        modifier = Modifier.padding(
                            top = 20.dp, bottom = 10.dp, start = 20.dp, end = 20.dp
                        ), style = MaterialTheme.typography.h6, text = titleObj
                    )
                }
            }

            items(items = list, key = { it.id }, contentType = { LSong::class }) { item ->
                SongCard(modifier = Modifier.animateItemPlacement(),
                    song = { item },
                    lyricRepository = playingVM.lyricRepository,
                    onClick = {
                        if (selector.isSelecting.value) {
                            selector.onSelected(item)
                        } else {
                            playingVM.playSongWithPlaylist(songs.values.flatten(), item)
                        }
                    },
                    onLongClick = { navToSongAction(item.id) },
                    onEnterSelect = { selector.onSelected(item) },
                    isSelected = { selectedItems.any { it.id == item.id } })
            }
        }
    }
}

@Composable
fun SortBar(
    libraryVM: LibraryViewModel, sortFor: String = Sortable.SORT_FOR_SONGS
) {
    var sortRule by rememberDataSaverState(
        key = "${sortFor}_SORT_RULE", default = SortRule.Normal.name
    )
    var orderRule by rememberDataSaverState(
        key = "${sortFor}_ORDER_RULE", default = OrderRule.ASC.name
    )
    var groupRule by rememberDataSaverState(
        key = "${sortFor}_GROUP_RULE", default = GroupRule.Normal.name
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        Column {
            libraryVM.supportSortRules.forEach {
                Text(
                    modifier = Modifier
                        .background(
                            color = if (it.name == sortRule) dayNightTextColor(
                                0.2f
                            ) else Color.Transparent
                        )
                        .padding(vertical = 5.dp)
                        .clickable { sortRule = it.name },
                    text = it.name
                )
            }
        }
        Column {
            libraryVM.supportOrderRules.forEach {
                Text(
                    modifier = Modifier
                        .background(
                            color = if (it.name == orderRule) dayNightTextColor(
                                0.2f
                            ) else Color.Transparent
                        )
                        .padding(vertical = 5.dp)
                        .clickable { orderRule = it.name },
                    text = it.name
                )
            }
        }
        Column {
            libraryVM.supportGroupRules.forEach {
                Text(
                    modifier = Modifier
                        .background(
                            color = if (it.name == groupRule) dayNightTextColor(
                                0.2f
                            ) else Color.Transparent
                        )
                        .padding(vertical = 5.dp)
                        .clickable { groupRule = it.name },
                    text = it.name
                )
            }
        }
    }
}