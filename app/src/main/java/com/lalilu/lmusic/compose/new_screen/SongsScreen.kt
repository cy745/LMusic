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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
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
import com.lalilu.lmusic.compose.BottomSheetWrapper
import com.lalilu.lmusic.compose.component.SmartBar
import com.lalilu.lmusic.compose.component.SmartContainer
import com.lalilu.lmusic.compose.component.SmartFloatBtns
import com.lalilu.lmusic.compose.component.base.SongsSelectWrapper
import com.lalilu.lmusic.compose.component.base.SortPanel
import com.lalilu.lmusic.compose.component.base.SortPreset
import com.lalilu.lmusic.compose.component.card.SongCard
import com.lalilu.lmusic.compose.component.navigate.NavigatorHeader
import com.lalilu.lmusic.compose.new_screen.destinations.SongDetailScreenDestination
import com.lalilu.lmusic.datastore.SettingsSp
import com.lalilu.lmusic.utils.extension.getIds
import com.lalilu.lmusic.viewmodel.HistoryViewModel
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
    historyVM: HistoryViewModel = get(),
    navigator: DestinationsNavigator
) {
    val scope = rememberCoroutineScope()
    val gridState = rememberLazyGridState()
    val songsState by songsVM.songsState
    val currentPlaying by playingVM.runtime.playingFlow.collectAsState(null)

    val showSortPanel = remember { mutableStateOf(false) }
    val scrollProgress = remember(gridState) {
        derivedStateOf {
            if (gridState.layoutInfo.totalItemsCount == 0) return@derivedStateOf 0f
            gridState.firstVisibleItemIndex / gridState.layoutInfo.totalItemsCount.toFloat()
        }
    }

    val supportSortPresets = remember {
        listOf(
            SortPreset.SortByAddTime,
            SortPreset.SortByTitle,
            SortPreset.SortByLastPlayTime,
            SortPreset.SortByPlayedTimes,
            SortPreset.SortByDuration
        )
    }
    val supportSortRules = remember {
        listOf(
            SortRule.Normal,
            SortRule.Title,
            SortRule.CreateTime,
            SortRule.ModifyTime,
            SortRule.ItemsDuration,
            SortRule.PlayCount,
            SortRule.LastPlayTime
        )
    }
    val supportGroupRules = remember {
        listOf(
            GroupRule.Normal,
            GroupRule.CreateTime,
            GroupRule.ModifyTime,
            GroupRule.PinYinFirstLetter,
            GroupRule.TitleFirstLetter
        )
    }
    val supportOrderRules = remember {
        listOf(
            OrderRule.Normal,
            OrderRule.Reverse,
            OrderRule.Shuffle
        )
    }
    LaunchedEffect(mediaIdsText) {
        songsVM.updateByIds(
            songIds = mediaIdsText.getIds(),
            sortFor = sortFor,
            supportSortRules = supportSortRules,
            supportOrderRules = supportOrderRules,
            supportGroupRules = supportGroupRules
        )
    }

    SmartFloatBtns.RegisterFloatBtns(
        progress = scrollProgress,
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
                                val tempIndex = list.indexOfFirst { it.id == currentPlaying!!.mediaId }
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
                callback = { scope.launch { gridState.scrollToItem(gridState.layoutInfo.totalItemsCount) } }
            )
        )
    )

    SortPanelWrapper(
        sortFor = sortFor,
        showPanelState = showSortPanel,
        supportSortPresets = { supportSortPresets },
        supportGroupRules = { supportGroupRules },
        supportOrderRules = { supportOrderRules },
        supportSortRules = { supportSortRules }
    ) { sortRuleStr ->
        SongListWrapper(
            state = gridState,
            songsState = songsState,
            hasLyricState = { playingVM.requireHasLyricState(item = it) },
            isItemPlaying = { playingVM.isSongPlaying(mediaId = it.id) },
            onClickItem = {
                playingVM.play(
                    song = it,
                    songs = songsState.values.flatten(),
                    playOrPause = true
                )
            },
            onLongClickItem = { navigator.navigate(SongDetailScreenDestination(mediaId = it.id)) },
            showPrefixContent = { sortRuleStr.value == SortRule.TrackNumber.name || sortRuleStr.value == SortRule.PlayCount.name },
            prefixContent = { item ->
                var icon = -1
                var text = ""
                when (sortRuleStr.value) {
                    SortRule.TrackNumber.name -> {
                        icon = R.drawable.ic_music_line
                        text = item.track.toString()
                    }

                    SortRule.PlayCount.name -> {
                        icon = R.drawable.headphone_fill
                        text = historyVM.requiteHistoryCountById(item.id).toString()
                    }
                }
                if (icon != -1) {
                    Icon(
                        modifier = Modifier.size(10.dp),
                        painter = painterResource(id = icon),
                        contentDescription = ""
                    )
                }
                if (text.isNotEmpty()) {
                    Text(
                        text = text,
                        fontSize = 12.sp
                    )
                }
            }
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
    settingsSp: SettingsSp = get(),
    showPanelState: MutableState<Boolean> = remember { mutableStateOf(false) },
    supportSortPresets: () -> List<SortPreset>,
    supportGroupRules: () -> List<GroupRule>,
    supportSortRules: () -> List<SortRule>,
    supportOrderRules: () -> List<OrderRule>,
    content: @Composable (sortRuleStr: State<String>) -> Unit
) {
    val sortRule = settingsSp.stringSp("${sortFor}_SORT_RULE", SortRule.Normal.name)
    val orderRule = settingsSp.stringSp("${sortFor}_ORDER_RULE", OrderRule.Normal.name)
    val groupRule = settingsSp.stringSp("${sortFor}_GROUP_RULE", GroupRule.Normal.name)

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

        BottomSheetWrapper.BackHandler(enable = { showPanelState.value }) {
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
    isItemPlaying: (LSong) -> Boolean = { false },
    showPrefixContent: () -> Boolean = { false },
    prefixContent: @Composable (item: LSong) -> Unit = {},
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
                        showPrefix = showPrefixContent,
                        prefixContent = { modifier ->
                            Row(
                                modifier = modifier
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colors.surface)
                                    .padding(start = 4.dp, end = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                prefixContent(item)
                            }
                        }
                    )
                }
            }
        }
    }
}