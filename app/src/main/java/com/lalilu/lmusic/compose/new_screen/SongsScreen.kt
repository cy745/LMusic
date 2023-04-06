package com.lalilu.lmusic.compose.new_screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lalilu.R
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmedia.extension.GroupIdentity
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
import com.lalilu.lmusic.compose.component.base.SortPreset
import com.lalilu.lmusic.compose.component.card.SongCard
import com.lalilu.lmusic.compose.component.navigate.NavigatorHeader
import com.lalilu.lmusic.compose.new_screen.destinations.SongDetailScreenDestination
import com.lalilu.lmusic.datastore.LMusicSp
import com.lalilu.lmusic.utils.extension.getIds
import com.lalilu.lmusic.viewmodel.PlayingViewModel
import com.lalilu.lmusic.viewmodel.SongsViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.launch
import org.koin.androidx.compose.get

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
                callback = { showAll ->
                    showSortPanel.value = true
                    showAll.value = false
                }
            ),
            SmartFloatBtns.FloatBtnItem(
                icon = R.drawable.ic_focus_3_line,
                title = "定位当前播放歌曲",
                callback = {
                    if (currentPlaying != null) {
                        scope.launch {
                            var index = 0
                            for ((identity, list) in songsState) {
                                val skip = identity == GroupIdentity.None ||
                                        identity is GroupIdentity.DiskNumber && identity.number < 0

                                if (!skip) {
                                    index += 1
                                }
                                val tempIndex = list.indexOfFirst { it.id == currentPlaying!!.id }
                                if (tempIndex != -1) {
                                    index += tempIndex
                                    gridState.scrollToItem(index)
                                    break
                                }

                                index += list.size
                            }
                        }
                    }
                }
            ),
            SmartFloatBtns.FloatBtnItem(
                icon = R.drawable.ic_arrow_up_s_line,
                title = "回到顶部",
                callback = { scope.launch { gridState.scrollToItem(0) } }
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
        supportSortPresets = {
            listOf(
                SortPreset.SortByAddTime,
                SortPreset.SortByTitle,
                SortPreset.SortByLastPlayTime,
                SortPreset.SortByPlayedTimes,
                SortPreset.SortByDuration
            )
        },
        supportGroupRules = { songsVM.sorter.supportGroupRules },
        supportOrderRules = { songsVM.sorter.supportOrderRules },
        supportSortRules = { songsVM.sorter.supportSortRules }
    ) {
        SongListWrapper(
            state = gridState,
            songsState = songsState,
            hasLyricState = { playingVM.requireHasLyricState(item = it) },
            isItemPlaying = { playingVM.isSongPlaying(mediaId = it.id) },
            onClickItem = { playingVM.playSongWithPlaylist(songsState.values.flatten(), it) },
            onLongClickItem = { navigator.navigate(SongDetailScreenDestination(mediaId = it.id)) }
        ) {
            item {
                NavigatorHeader(
                    title = title,
                    subTitle = "共 ${songsState.values.flatten().size} 首歌曲"
                )
            }
        }
    }
}

@Composable
fun SortPanelWrapper(
    sortFor: String,
    lMusicSp: LMusicSp = get(),
    showPanelState: MutableState<Boolean> = remember { mutableStateOf(false) },
    supportSortPresets: () -> List<SortPreset>,
    supportGroupRules: () -> List<GroupRule>,
    supportSortRules: () -> List<SortRule>,
    supportOrderRules: () -> List<OrderRule>,
    content: @Composable (State<String>) -> Unit
) {
    val sortRule = lMusicSp.stringSp("${sortFor}_SORT_RULE", SortRule.Normal.name)
    val orderRule = lMusicSp.stringSp("${sortFor}_ORDER_RULE", OrderRule.Normal.name)
    val groupRule = lMusicSp.stringSp("${sortFor}_GROUP_RULE", GroupRule.Normal.name)

    SmartBar.RegisterMainBarContent(
        showState = showPanelState,
        showMask = true,
        showBackground = false
    ) {
        SortPanel(
            sortRule = sortRule,
            orderRule = orderRule,
            groupRule = groupRule,
            supportSortPresets = supportSortPresets,
            supportGroupRules = supportGroupRules,
            supportOrderRules = supportOrderRules,
            supportSortRules = supportSortRules,
            onClose = { showPanelState.value = false }
        )
        BackHandler(showPanelState.value && SmartModalBottomSheet.isVisible.value) {
            showPanelState.value = false
        }
    }
    content(sortRule)
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SongListWrapper(
    state: LazyGridState = rememberLazyGridState(),
    songsState: Map<GroupIdentity, List<LSong>>,
    hasLyricState: (LSong) -> State<Boolean>,
    onClickItem: (LSong) -> Unit = {},
    onLongClickItem: (LSong) -> Unit = {},
    showTrackNumber: () -> Boolean = { false },
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

            for ((groupIdentity, list) in songsState) {
                var skip = groupIdentity == GroupIdentity.None
                skip = skip || groupIdentity is GroupIdentity.DiskNumber && groupIdentity.number < 0

                if (!skip) {
                    item(
                        key = groupIdentity,
                        contentType = GroupIdentity::class,
                        span = { GridItemSpan(maxLineSpan) }
                    ) {
                        Text(
                            modifier = Modifier.padding(
                                top = 20.dp,
                                bottom = 10.dp,
                                start = 20.dp,
                                end = 20.dp
                            ),
                            style = MaterialTheme.typography.h6,
                            text = when (groupIdentity) {
                                is GroupIdentity.Time -> groupIdentity.time
                                is GroupIdentity.DiskNumber -> groupIdentity.number.toString()
                                is GroupIdentity.FirstLetter -> groupIdentity.letter
                                else -> ""
                            }
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
                        isPlaying = { isItemPlaying(item) },
                        showPrefix = showTrackNumber,
                        prefixContent = { modifier ->
                            Row(
                                modifier = modifier
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colors.surface)
                                    .padding(start = 4.dp, end = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Icon(
                                    modifier = Modifier.size(10.dp),
                                    painter = painterResource(id = R.drawable.headphone_fill),
                                    contentDescription = ""
                                )
                                Text(
                                    text = item.track.toString(),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}