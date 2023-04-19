package com.lalilu.lmusic.compose.new_screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lalilu.R
import com.lalilu.lmedia.entity.LAlbum
import com.lalilu.lmedia.extension.GroupRule
import com.lalilu.lmedia.extension.OrderRule
import com.lalilu.lmedia.extension.SortRule
import com.lalilu.lmedia.extension.Sortable
import com.lalilu.lmusic.compose.component.SmartContainer
import com.lalilu.lmusic.compose.component.SmartFloatBtns
import com.lalilu.lmusic.compose.component.base.SortPreset
import com.lalilu.lmusic.compose.component.card.AlbumCard
import com.lalilu.lmusic.compose.component.navigate.NavigatorHeader
import com.lalilu.lmusic.compose.new_screen.destinations.AlbumDetailScreenDestination
import com.lalilu.lmusic.datastore.LMusicSp
import com.lalilu.lmusic.utils.extension.getIds
import com.lalilu.lmusic.viewmodel.AlbumsViewModel
import com.lalilu.lmusic.viewmodel.PlayingViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.launch
import org.koin.androidx.compose.get

@OptIn(ExperimentalFoundationApi::class)
@HomeNavGraph
@Destination
@Composable
fun AlbumsScreen(
    title: String = "全部专辑",
    sortFor: String = Sortable.SORT_FOR_ALBUMS,
    albumIdsText: String? = null,
    lMusicSp: LMusicSp = get(),
    playingVM: PlayingViewModel = get(),
    albumsVM: AlbumsViewModel = get(),
    navigator: DestinationsNavigator
) {
    val albums by albumsVM.albums
    val currentPlaying by playingVM.currentPlaying.observeAsState()

    val scope = rememberCoroutineScope()
    val gridState = rememberLazyStaggeredGridState()
    val showSortPanel = remember { mutableStateOf(false) }
    val showTitleState = lMusicSp.boolSp("show_album_title", true)

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
            SortRule.FileSize,
            SortRule.PlayCount,
            SortRule.LastPlayTime
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

    val scrollProgress = remember(gridState) {
        derivedStateOf {
            if (gridState.layoutInfo.totalItemsCount == 0) return@derivedStateOf 0f
            gridState.firstVisibleItemIndex / gridState.layoutInfo.totalItemsCount.toFloat()
        }
    }

    LaunchedEffect(albumIdsText) {
        albumsVM.updateByIds(
            ids = albumIdsText.getIds(),
            sortFor = sortFor,
            supportSortRules = supportSortRules,
            supportGroupRules = supportGroupRules,
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
                    showSortPanel.value = true
                    showAll.value = false
                }
            ),
            SmartFloatBtns.FloatBtnItem(
                title = "显示文字",
                icon = R.drawable.ic_text
            ) {
                showTitleState.value = !showTitleState.value
            },
            SmartFloatBtns.FloatBtnItem(
                icon = R.drawable.ic_focus_3_line,
                title = "定位当前播放专辑",
                callback = {
                    if (currentPlaying != null) {
                        scope.launch {
                            val index = albums.indexOfFirst { it.id == currentPlaying?.album?.id }
                            if (index in 0..gridState.layoutInfo.totalItemsCount) {
                                gridState.scrollToItem(index)
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
    ) {
        SmartContainer.LazyStaggeredVerticalGrid(
            staggeredGridState = gridState,
            contentPaddingForHorizontal = 10.dp,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            columns = { if (it == WindowWidthSizeClass.Expanded) 3 else 2 }
        ) {
            item(key = "Header", contentType = "Header") {
                Surface(shape = RoundedCornerShape(5.dp)) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                    ) {
                        NavigatorHeader(
                            title = title,
                            subTitle = "共 ${albums.size} 张专辑"
                        )
                    }
                }
            }
            items(items = albums, key = { it.id }, contentType = { LAlbum::class }) {
                AlbumCard(
                    album = { it },
                    isPlaying = { playingVM.isAlbumPlaying(it.id) },
                    showTitle = { showTitleState.value },
                    onClick = { navigator.navigate(AlbumDetailScreenDestination(it.id)) }
                )
            }
        }
    }
}