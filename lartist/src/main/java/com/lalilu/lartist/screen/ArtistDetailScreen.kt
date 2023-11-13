package com.lalilu.lartist.screen

import androidx.compose.runtime.Composable
import com.lalilu.component.base.DynamicScreen
import com.lalilu.component.base.ScreenInfo
import com.lalilu.lartist.R

data class ArtistDetailScreen(
    private val artistName: String
) : DynamicScreen() {

    override fun getScreenInfo(): ScreenInfo = ScreenInfo(
        title = R.string.artist_screen_title,
    )

    @Composable
    override fun Content() {
//        ArtistDetail(artistName = artistName)
    }
}

//@Composable
//private fun DynamicScreen.ArtistDetail(
//    artistName: String,
//    mediaVM: LMediaViewModel = singleViewModel(),
//    historyVM: HistoryViewModel = singleViewModel(),
//) {
//    val artist = mediaVM.requireArtist(artistName) ?: run {
//        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//            Text(text = "[Error]加载失败 #$artistName")
//        }
//        return
//    }
//    val sortFor = remember { "ArtistDetail" }
//    val listState = rememberLazyListState()
//
////    val supportSortPresets = remember {
////        listOf(
////            SortPreset.SortByAddTime,
////            SortPreset.SortByTitle,
////            SortPreset.SortByLastPlayTime,
////            SortPreset.SortByPlayedTimes,
////            SortPreset.SortByDuration
////        )
////    }
////    val supportSortRules = remember {
////        listOf(
////            SortRule.Normal,
////            SortRule.Title,
////            SortRule.CreateTime,
////            SortRule.ModifyTime,
////            SortRule.ItemsDuration,
////            SortRule.PlayCount,
////            SortRule.LastPlayTime
////        )
////    }
////    val supportGroupRules = remember {
////        listOf(
////            GroupRule.Normal,
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
//        mediaIds = artist.songs.map { it.mediaId },
//        listState = listState,
//        showPrefixContent = {
//            it.value == SortRuleStatic.TrackNumber::class.java.name ||
//                    it.value == "SortRulePlayCount"
//        },
//        supportListAction = { emptyList() },
//        headerContent = {
//            item {
//                NavigatorHeader(
//                    title = artist.name,
//                    subTitle = "共 ${it.value.values.flatten().size} 首歌曲，总时长 ${
//                        artist.requireItemsDuration().durationToTime()
//                    }"
//                )
//            }
//        },
//        prefixContent = { item, sortRuleStr ->
//            var icon = -1
//            var text = ""
//            when (sortRuleStr.value) {
//                "SortRulePlayCount" -> {
//                    icon = ComponentR.drawable.headphone_fill
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
//                    fontSize = 12.sp
//                )
//            }
//        }
//    )
//}