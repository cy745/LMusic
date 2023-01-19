package com.lalilu.lmusic.compose.screen.library

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.Button
import androidx.compose.material.ChipDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FilterChip
import androidx.compose.material.Icon
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import com.blankj.utilcode.util.TimeUtils
import com.funny.data_saver.core.rememberDataSaverState
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.navigation.animation.composable
import com.lalilu.R
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmedia.extension.GroupRule
import com.lalilu.lmedia.extension.OrderRule
import com.lalilu.lmedia.extension.SortRule
import com.lalilu.lmedia.extension.Sortable
import com.lalilu.lmusic.compose.component.SmartBar
import com.lalilu.lmusic.compose.component.SmartContainer
import com.lalilu.lmusic.compose.component.base.IconTextButton
import com.lalilu.lmusic.compose.component.card.SongCard
import com.lalilu.lmusic.compose.screen.BaseScreen
import com.lalilu.lmusic.compose.screen.LibraryDetailNavigateBar
import com.lalilu.lmusic.compose.screen.ScreenData
import com.lalilu.lmusic.compose.screen.library.detail.SongDetailScreen
import com.lalilu.lmusic.utils.extension.LocalWindowSize
import com.lalilu.lmusic.utils.extension.buildScrollToItemAction
import com.lalilu.lmusic.utils.rememberSelectState
import com.lalilu.lmusic.viewmodel.*

@OptIn(ExperimentalAnimationApi::class)
object SongsScreen : BaseScreen() {
    override fun register(builder: NavGraphBuilder) {
        builder.composable(ScreenData.Songs.name) {
            SongsScreen()
        }
    }

    override fun getNavToRoute(): String {
        return ScreenData.Songs.name
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SongsScreen(
    mainVM: MainViewModel = LocalMainVM.current,
    libraryVM: LibraryViewModel = LocalLibraryVM.current,
    playingVM: PlayingViewModel = LocalPlayingVM.current,
) {
    val windowSize = LocalWindowSize.current
    val gridState = rememberLazyGridState()

    val navToSongAction = SongDetailScreen.navToByArgv(hapticType = HapticFeedbackType.LongPress)
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
                    IconTextButton(
                        text = "取消",
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

@OptIn(ExperimentalMaterialApi::class, ExperimentalAnimationApi::class)
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(text = "排序依据")
        FlowRow(mainAxisSpacing = 8.dp) {
            libraryVM.supportSortRules.forEach { item ->
                FilterChip(
                    onClick = { sortRule = item.name },
                    selected = item.name == sortRule,
                    colors = ChipDefaults.outlinedFilterChipColors(),
                    leadingIcon = {
                        AnimatedContent(
                            targetState = item.name == sortRule,
                            transitionSpec = { fadeIn() with fadeOut() }
                        ) { show ->
                            if (show) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_checkbox_circle_line),
                                    contentDescription = ""
                                )
                            } else {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_checkbox_blank_circle_line),
                                    contentDescription = ""
                                )
                            }
                        }
                    }
                ) {
                    Text(text = item.title)
                }
            }
        }
        Text(text = "分组依据")
        FlowRow(mainAxisSpacing = 8.dp) {
            libraryVM.supportGroupRules.forEach { item ->
                FilterChip(
                    onClick = { groupRule = item.name },
                    selected = item.name == groupRule,
                    colors = ChipDefaults.outlinedFilterChipColors(),
                    leadingIcon = {
                        AnimatedContent(
                            targetState = item.name == groupRule,
                            transitionSpec = { fadeIn() with fadeOut() }
                        ) { show ->
                            if (show) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_checkbox_circle_line),
                                    contentDescription = ""
                                )
                            } else {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_checkbox_blank_circle_line),
                                    contentDescription = ""
                                )
                            }
                        }
                    }
                ) {
                    Text(text = item.title)
                }
            }
        }
        Text(text = "排序顺序")
        FlowRow(mainAxisSpacing = 8.dp) {
            libraryVM.supportOrderRules.forEach { item ->
                FilterChip(
                    onClick = { orderRule = item.name },
                    selected = item.name == orderRule,
                    colors = ChipDefaults.outlinedFilterChipColors(),
                    leadingIcon = {
                        AnimatedContent(
                            targetState = item.name == orderRule,
                            transitionSpec = { fadeIn() with fadeOut() }
                        ) { show ->
                            if (show) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_checkbox_circle_line),
                                    contentDescription = ""
                                )
                            } else {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_checkbox_blank_circle_line),
                                    contentDescription = ""
                                )
                            }
                        }
                    }
                ) {
                    Text(text = item.title)
                }
            }
        }
    }
}