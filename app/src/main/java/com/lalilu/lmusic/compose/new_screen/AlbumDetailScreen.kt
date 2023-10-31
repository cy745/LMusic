package com.lalilu.lmusic.compose.new_screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lalilu.R
import com.lalilu.lmedia.extension.GroupRule
import com.lalilu.lmedia.extension.OrderRule
import com.lalilu.lmedia.extension.SortRule
import com.lalilu.lmusic.compose.DynamicScreen
import com.lalilu.lmusic.compose.ScreenInfo
import com.lalilu.lmusic.compose.component.base.SortPreset
import com.lalilu.lmusic.compose.component.card.AlbumCoverCard
import com.lalilu.lmusic.compose.component.navigate.NavigatorHeader
import com.lalilu.lmusic.utils.extension.durationToTime
import com.lalilu.lmusic.utils.extension.singleViewModel
import com.lalilu.lmusic.viewmodel.HistoryViewModel
import com.lalilu.lmusic.viewmodel.LMediaViewModel

data class AlbumDetailScreen(
    private val albumId: String
) : DynamicScreen() {

    override fun getScreenInfo(): ScreenInfo = ScreenInfo(
        title = R.string.screen_title_album_detail,
    )

    @Composable
    override fun Content() {
        AlbumDetail(albumId = albumId)
    }
}

@Composable
private fun DynamicScreen.AlbumDetail(
    albumId: String,
    historyVM: HistoryViewModel = singleViewModel(),
    mediaVM: LMediaViewModel = singleViewModel(),
) {
    val album = mediaVM.requireAlbum(albumId) ?: run {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "[Error]加载失败 #$albumId")
        }
        return
    }

    val listState = rememberLazyListState()
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

    Songs(
        sortFor = sortFor,
        mediaIds = album.songs.map { it.mediaId },
        listState = listState,
        supportGroupRules = supportGroupRules,
        supportSortRules = supportSortRules,
        supportOrderRules = supportOrderRules,
        supportSortPresets = supportSortPresets,
        headerContent = {
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
                    subTitle = "共 ${it.value.values.flatten().size} 首歌曲，总时长 ${
                        album.requireItemsDuration().durationToTime()
                    }"
                )
            }
        },
        prefixContent = { item, sortRuleStr ->
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
                    fontSize = 10.sp
                )
            }
        }
    )
}