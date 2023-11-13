package com.lalilu.lalbum.screen

import androidx.compose.runtime.Composable
import com.lalilu.lalbum.R
import com.lalilu.component.base.DynamicScreen
import com.lalilu.component.base.ScreenInfo

data class AlbumDetailScreen(
    private val albumId: String
) : DynamicScreen() {

    override fun getScreenInfo(): ScreenInfo = ScreenInfo(
        title = R.string.album_screen_title,
    )

    @Composable
    override fun Content() {
//        AlbumDetail(albumId = albumId)
    }
}

//
//@Composable
//private fun DynamicScreen.AlbumDetail(
//    albumId: String,
//    historyVM: HistoryViewModel = singleViewModel(),
//    mediaVM: LMediaViewModel = singleViewModel(),
//) {
//    val album = mediaVM.requireAlbum(albumId) ?: run {
//        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//            Text(text = "[Error]加载失败 #$albumId")
//        }
//        return
//    }
//
//    val listState = rememberLazyListState()
//    val sortFor = remember { "AlbumDetail" }
////    val supportSortPresets = remember {
////        listOf(
////            SortPreset.SortByAddTime,
////            SortPreset.SortByTitle,
//////            SortPreset.SortByLastPlayTime,
//////            SortPreset.SortByPlayedTimes,
////            SortPreset.SortByDuration,
////            SortPreset.SortByDiskAndTrackNumber
////        )
////    }
////    val supportSortRules = remember {
////        listOf(
////            SortRule.Normal,
////            SortRule.Title,
////            SortRule.CreateTime,
////            SortRule.ModifyTime,
////            SortRule.TrackNumber,
////            SortRule.ItemsCount,
////            SortRule.ItemsDuration,
////            SortRule.PlayCount,
////            SortRule.LastPlayTime
////        )
////    }
////    val supportGroupRules = remember {
////        listOf(
////            GroupRule.DiskNumber,
////            GroupRule.CreateTime,
////            GroupRule.ModifyTime,
////            GroupRule.PinYinFirstLetter,
////            GroupRule.TitleFirstLetter
////        )
////    }
////    val supportOrderRules = remember {
////        listOf(
////            OrderRule.Normal,
////            OrderRule.Reverse,
////            OrderRule.Shuffle
////        )
////    }
//
//    Songs(
//        sortFor = sortFor,
//        mediaIds = album.songs.map { it.mediaId },
//        listState = listState,
//        showPrefixContent = {
//            it.value == SortRuleStatic.TrackNumber::class.java.name ||
//                    it.value == SortRulePlayCount::class.java.name
//        },
//        supportListAction = { emptyList() },
//        headerContent = {
//            item {
//                AlbumCoverCard(
//                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
//                    shape = RoundedCornerShape(10.dp),
//                    elevation = 2.dp,
//                    imageData = { album },
//                    onClick = { }
//                )
//            }
//
//            item {
//                NavigatorHeader(
//                    title = album.name,
//                    subTitle = "共 ${it.value.values.flatten().size} 首歌曲，总时长 ${
//                        album.requireItemsDuration().durationToTime()
//                    }"
//                )
//            }
//        },
//        prefixContent = { item, sortRuleStr ->
//            var icon = -1
//            var text = ""
//            when (sortRuleStr.value) {
//                SortRuleStatic.TrackNumber::class.java.name -> {
//                    icon = R.drawable.ic_music_line
//                    text = if (item is LSong) item.track.toString() else ""
//                }
//
//                SortRulePlayCount::class.java.name -> {
//                    icon = R.drawable.headphone_fill
//                    text = historyVM.requiteHistoryCountById(item.mediaId).toString()
//                }
//            }
//            if (icon != -1) {
//                Icon(
//                    modifier = Modifier.size(10.dp),
//                    painter = painterResource(id = icon),
//                    contentDescription = ""
//                )
//            }
//            if (text.isNotEmpty()) {
//                Text(
//                    text = text,
//                    fontSize = 10.sp
//                )
//            }
//        }
//    )
//}