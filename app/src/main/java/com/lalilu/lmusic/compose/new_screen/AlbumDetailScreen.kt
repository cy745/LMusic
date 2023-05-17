package com.lalilu.lmusic.compose.new_screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lalilu.R
import com.lalilu.lmedia.extension.GroupIdentity
import com.lalilu.lmedia.extension.GroupRule
import com.lalilu.lmedia.extension.OrderRule
import com.lalilu.lmedia.extension.SortRule
import com.lalilu.lmusic.compose.component.SmartFloatBtns
import com.lalilu.lmusic.compose.component.base.SortPreset
import com.lalilu.lmusic.compose.component.card.AlbumCoverCard
import com.lalilu.lmusic.compose.component.navigate.NavigatorHeader
import com.lalilu.lmusic.compose.new_screen.destinations.SongDetailScreenDestination
import com.lalilu.lmusic.utils.extension.durationToTime
import com.lalilu.lmusic.viewmodel.HistoryViewModel
import com.lalilu.lmusic.viewmodel.LMediaViewModel
import com.lalilu.lmusic.viewmodel.PlayingViewModel
import com.lalilu.lmusic.viewmodel.SongsViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.launch
import org.koin.androidx.compose.get

@Destination
@Composable
fun AlbumDetailScreen(
    albumId: String,
    playingVM: PlayingViewModel = get(),
    historyVM: HistoryViewModel = get(),
    mediaVM: LMediaViewModel = get(),
    songsVM: SongsViewModel = get(),
    navigator: DestinationsNavigator
) {
    val album = mediaVM.requireAlbum(albumId) ?: run {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "[Error]加载失败 #$albumId")
        }
        return
    }

    val currentPlaying by playingVM.currentPlaying.observeAsState()
    val scope = rememberCoroutineScope()
    val showSortPanel = remember { mutableStateOf(false) }
    val songsState by songsVM.songsState
    val gridState = rememberLazyGridState()
    val sortFor = remember { "AlbumDetail" }
    val supportSortPresets = remember {
        listOf(
            SortPreset.SortByAddTime,
            SortPreset.SortByTitle,
            SortPreset.SortByLastPlayTime,
            SortPreset.SortByPlayedTimes,
            SortPreset.SortByDuration,
            SortPreset.SortByDiskAndTrackNumber
        )
    }
    val supportSortRules = remember {
        listOf(
            SortRule.Normal,
            SortRule.Title,
            SortRule.CreateTime,
            SortRule.ModifyTime,
            SortRule.TrackNumber,
            SortRule.ItemsCount,
            SortRule.ItemsDuration,
            SortRule.PlayCount,
            SortRule.LastPlayTime
        )
    }
    val supportGroupRules = remember {
        listOf(
            GroupRule.DiskNumber,
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

    LaunchedEffect(album) {
        songsVM.updateBySongs(
            songs = album.songs,
            sortFor = sortFor,
            supportGroupRules = supportGroupRules,
            supportSortRules = supportSortRules,
            supportOrderRules = supportOrderRules
        )
    }

    val scrollProgress = remember(gridState) {
        derivedStateOf {
            if (gridState.layoutInfo.totalItemsCount == 0) return@derivedStateOf 0f
            gridState.firstVisibleItemIndex / gridState.layoutInfo.totalItemsCount.toFloat()
        }
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
                callback = { scope.launch { gridState.scrollToItem(gridState.layoutInfo.totalItemsCount) } }
            )
        )
    )


    SortPanelWrapper(
        sortFor = sortFor,
        showPanelState = showSortPanel,
        supportSortPresets = { supportSortPresets },
        supportGroupRules = { supportGroupRules },
        supportSortRules = { supportSortRules },
        supportOrderRules = { supportOrderRules }
    ) { sortRuleStr ->
        SongListWrapper(
            state = gridState,
            songsState = songsState,
            showPrefixContent = { sortRuleStr.value == SortRule.TrackNumber.name || sortRuleStr.value == SortRule.PlayCount.name },
            isItemPlaying = { playingVM.isSongPlaying(mediaId = it.id) },
            hasLyricState = { playingVM.requireHasLyricState(item = it) },
            onLongClickItem = { navigator.navigate(SongDetailScreenDestination(mediaId = it.id)) },
            onClickItem = {
                playingVM.play(
                    song = it,
                    songs = songsState.values.flatten(),
                    playOrPause = true
                )
            },
            prefixContent = { item ->
                // TODO 待优化代码逻辑, 使其更加简洁
                var icon = -1
                var text = ""
                when (sortRuleStr.value) {
                    SortRule.TrackNumber.name -> {
                        icon = R.drawable.ic_music_line
                        text = item.track?.toString() ?: ""
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
                Text(
                    text = text,
                    fontSize = 12.sp
                )
            }
        ) {
            item {
                AlbumCoverCard(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                    shape = RoundedCornerShape(10.dp),
                    elevation = 2.dp,
                    imageData = { album },
                    onClick = { }
                )
            }

            item {
                NavigatorHeader(
                    title = album.name,
                    subTitle = "共 ${songsState.values.flatten().size} 首歌曲，总时长 ${
                        album.requireItemsDuration().durationToTime()
                    }"
                )
            }
        }
    }
}