package com.lalilu.lalbum.screen

import androidx.compose.runtime.Composable
import com.lalilu.lalbum.R
import com.lalilu.component.R as ComponentR
import com.lalilu.component.base.DynamicScreen
import com.lalilu.component.base.ScreenInfo

data class AlbumsScreen(val albumsId: List<String> = emptyList()) : DynamicScreen() {
    override fun getScreenInfo(): ScreenInfo = ScreenInfo(
        title = R.string.album_screen_title,
        icon = ComponentR.drawable.ic_album_fill
    )

    @Composable
    override fun Content() {
//        AlbumsScreen()
    }
}

//@Composable
//private fun DynamicScreen.AlbumsScreen(
//    title: String = "全部专辑",
//    sortFor: String = Sortable.SORT_FOR_ALBUMS,
//    albumIdsText: String? = null,
//    settingsSp: SettingsSp = koinInject(),
//    playingVM: PlayingViewModel = singleViewModel(),
//    albumsVM: AlbumsViewModel = singleViewModel(),
//) {
//    val albums by albumsVM.albums
//    val currentPlaying by LPlayer.runtime.info.playingFlow.collectAsState(null)
//
//    val scope = rememberCoroutineScope()
//    val gridState = rememberLazyStaggeredGridState()
//    val showSortPanel = remember { mutableStateOf(false) }
//    val showTitleState = settingsSp.obtain<Boolean>("show_album_title", true)
//
////    val supportSortPresets = remember {
////        listOf(
////            SortPreset.SortByAddTime,
////            SortPreset.SortByTitle,
////            SortPreset.SortByDuration,
////            SortPreset.SortByItemCount
////        )
////    }
////    val supportSortRules = remember {
////        listOf(
////            SortRule.Normal,
////            SortRule.Title,
////            SortRule.ItemsCount,
////            SortRule.ItemsDuration,
////            SortRule.FileSize,
////            SortRule.PlayCount,
////            SortRule.LastPlayTime
////        )
////    }
////    val supportGroupRules = remember { emptyList<GroupRule>() }
////    val supportOrderRules = remember {
////        listOf(
////            OrderRule.Normal,
////            OrderRule.Reverse,
////            OrderRule.Shuffle
////        )
////    }
//
//    val scrollProgress = remember(gridState) {
//        derivedStateOf {
//            if (gridState.layoutInfo.totalItemsCount == 0) return@derivedStateOf 0f
//            gridState.firstVisibleItemIndex / gridState.layoutInfo.totalItemsCount.toFloat()
//        }
//    }
//
////    LaunchedEffect(albumIdsText) {
////        albumsVM.updateByIds(
////            ids = albumIdsText.getIds(),
////            sortFor = sortFor,
////            supportSortRules = supportSortRules,
////            supportGroupRules = supportGroupRules,
////            supportOrderRules = supportOrderRules
////        )
////    }
//
////    SmartFloatBtns.RegisterFloatBtns(
////        progress = scrollProgress,
////        items = listOf(
////            SmartFloatBtns.FloatBtnItem(
////                title = "排序",
////                icon = R.drawable.ic_sort_desc,
////                callback = { showAll ->
////                    showSortPanel.value = true
////                    showAll.value = false
////                }
////            ),
////            SmartFloatBtns.FloatBtnItem(
////                title = "显示文字",
////                icon = R.drawable.ic_text
////            ) {
////                showTitleState.value = !showTitleState.value
////            },
////            SmartFloatBtns.FloatBtnItem(
////                icon = R.drawable.ic_focus_3_line,
////                title = "定位当前播放专辑",
////                callback = {
////                    if (currentPlaying != null) {
////                        scope.launch {
////                            val index = albums.indexOfFirst {
////                                it.id == (currentPlaying as? LSong)?.album?.id
////                            }
////                            if (index in 0..gridState.layoutInfo.totalItemsCount) {
////                                gridState.scrollToItem(index)
////                            }
////                        }
////                    }
////                }
////            ),
////            SmartFloatBtns.FloatBtnItem(
////                icon = R.drawable.ic_arrow_up_s_line,
////                title = "回到顶部",
////                callback = { scope.launch { gridState.scrollToItem(0) } }
////            ),
////            SmartFloatBtns.FloatBtnItem(
////                icon = R.drawable.ic_arrow_down_s_line,
////                title = "滚动到底部",
////                callback = { scope.launch { gridState.scrollToItem(gridState.layoutInfo.totalItemsCount) } }
////            )
////        )
////    )
//
//    SortPanelWrapper(
//        sortFor = sortFor,
//        showPanelState = showSortPanel,
//        supportListAction = { emptyList() },
//        sp = koinInject<SettingsSp>()
//    ) {
//        SmartContainer.LazyStaggeredVerticalGrid(
//            staggeredGridState = gridState,
//            contentPaddingForHorizontal = 10.dp,
//            verticalArrangement = Arrangement.spacedBy(10.dp),
//            horizontalArrangement = Arrangement.spacedBy(10.dp),
//            columns = { if (it == WindowWidthSizeClass.Expanded) 3 else 2 }
//        ) {
//            item(key = "Header", contentType = "Header") {
//                Surface(shape = RoundedCornerShape(5.dp)) {
//                    Column(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(bottom = 10.dp)
//                    ) {
//                        NavigatorHeader(
//                            title = title,
//                            subTitle = "共 ${albums.size} 张专辑"
//                        )
//                    }
//                }
//            }
//            items(items = albums, key = { it.id }, contentType = { LAlbum::class }) { item ->
//                AlbumCard(
//                    album = { item },
//                    isPlaying = {
//                        playingVM.isItemPlaying { playing ->
//                            playing.let { it as? LSong }
//                                ?.let { it.album?.id == item.id }
//                                ?: false
//                        }
//                    },
//                    showTitle = { showTitleState.value },
//                    onClick = {
////                        navigator.navigate(AlbumDetailScreenDestination(item.id))
//                    }
//                )
//            }
//        }
//    }
//}