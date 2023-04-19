package com.lalilu.lmusic.compose.new_screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.lalilu.R
import com.lalilu.lmedia.entity.LArtist
import com.lalilu.lmedia.extension.GroupRule
import com.lalilu.lmedia.extension.OrderRule
import com.lalilu.lmedia.extension.SortRule
import com.lalilu.lmedia.extension.Sortable
import com.lalilu.lmusic.compose.component.SmartContainer
import com.lalilu.lmusic.compose.component.SmartFloatBtns
import com.lalilu.lmusic.compose.component.base.SortPreset
import com.lalilu.lmusic.compose.component.card.ArtistCard
import com.lalilu.lmusic.compose.component.navigate.NavigatorHeader
import com.lalilu.lmusic.compose.new_screen.destinations.ArtistDetailScreenDestination
import com.lalilu.lmusic.utils.extension.getIds
import com.lalilu.lmusic.viewmodel.ArtistsViewModel
import com.lalilu.lmusic.viewmodel.PlayingViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.launch
import org.koin.androidx.compose.get

@OptIn(ExperimentalFoundationApi::class)
@HomeNavGraph
@Destination
@Composable
fun ArtistsScreen(
    title: String = "所有艺术家",
    sortFor: String = Sortable.SORT_FOR_ARTISTS,
    artistIdsText: String? = null,
    playingVM: PlayingViewModel = get(),
    artistsVM: ArtistsViewModel = get(),
    navigator: DestinationsNavigator
) {
    val artists = artistsVM.artists
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val showPanelState = remember { mutableStateOf(false) }
    val currentPlaying by playingVM.currentPlaying.observeAsState()

    val scrollProgress = remember(listState) {
        derivedStateOf {
            if (listState.layoutInfo.totalItemsCount == 0) return@derivedStateOf 0f
            listState.firstVisibleItemIndex / listState.layoutInfo.totalItemsCount.toFloat()
        }
    }

    val supportSortPresets = remember {
        listOf(
            SortPreset.SortByAddTime,
            SortPreset.SortByTitle,
            SortPreset.SortByDuration,
            SortPreset.SortByItemCount
        )
    }
    val supportSortRules = remember {
        listOf(
            SortRule.Normal,
            SortRule.Title,
            SortRule.ItemsCount,
            SortRule.ItemsDuration,
            SortRule.FileSize
        )
    }
    val supportGroupRules = remember { emptyList<GroupRule>() }
    val supportOrderRules = remember {
        listOf(
            OrderRule.Normal,
            OrderRule.Reverse,
            OrderRule.Shuffle
        )
    }

    LaunchedEffect(artistIdsText) {
        artistsVM.updateByIds(
            sortFor = sortFor,
            ids = artistIdsText.getIds(),
            supportGroupRules = supportGroupRules,
            supportSortRules = supportSortRules,
            supportOrderRules = supportOrderRules
        )
    }

    SmartFloatBtns.RegisterFloatBtns(
        progress = scrollProgress,
        items = listOf(
            SmartFloatBtns.FloatBtnItem(
                title = "排序",
                icon = R.drawable.ic_sort_desc,
                callback = { showAll ->
                    showPanelState.value = true
                    showAll.value = false
                }
            ),
            SmartFloatBtns.FloatBtnItem(
                icon = R.drawable.ic_focus_3_line,
                title = "定位当前播放歌曲",
                callback = {
                    if (currentPlaying != null) {
                        scope.launch {
                            var targetIndex = -1
                            val startIndex = listState.firstVisibleItemIndex

                            // 从当前可见的元素Index开始往后找
                            for (i in startIndex until artists.value.size) {
                                if (i == startIndex) continue

                                if (currentPlaying!!.artists.any { it.name == artists.value[i].name }) {
                                    targetIndex = i
                                    break
                                }
                            }

                            // 若无法往后找到，则从头开始找
                            if (targetIndex == -1) {
                                targetIndex = artists.value.indexOfFirst { artist ->
                                    currentPlaying!!.artists.any { it.name == artist.name }
                                }
                            }

                            // 若找到则跳转
                            if (targetIndex != -1) {
                                listState.scrollToItem(targetIndex)
                            }
                        }
                    }
                }
            ),
            SmartFloatBtns.FloatBtnItem(
                icon = R.drawable.ic_arrow_up_s_line,
                title = "回到顶部",
                callback = { scope.launch { listState.scrollToItem(0) } }
            ),
            SmartFloatBtns.FloatBtnItem(
                icon = R.drawable.ic_arrow_down_s_line,
                title = "滚动到底部",
                callback = { scope.launch { listState.scrollToItem(listState.layoutInfo.totalItemsCount) } }
            )
        )
    )

    SortPanelWrapper(
        sortFor = sortFor,
        showPanelState = showPanelState,
        supportSortPresets = { supportSortPresets },
        supportGroupRules = { supportGroupRules },
        supportSortRules = { supportSortRules },
        supportOrderRules = { supportOrderRules },
    ) { sortRuleStr ->
        SmartContainer.LazyColumn(state = listState) {
            item(key = "Header") {
                NavigatorHeader(
                    title = title,
                    subTitle = "共 ${artists.value.size} 条记录"
                )
            }

            itemsIndexed(
                items = artists.value,
                key = { _, item -> item.id },
                contentType = { _, _ -> LArtist::class }
            ) { index, item ->
                ArtistCard(
                    modifier = Modifier.animateItemPlacement(),
                    index = index,
                    artistName = item.name,
                    songCount = item.requireItemsCount(),
                    isPlaying = { playingVM.isArtistPlaying(item.name) },
                    onClick = { navigator.navigate(ArtistDetailScreenDestination(item.name)) }
                )
            }
        }
    }
}
