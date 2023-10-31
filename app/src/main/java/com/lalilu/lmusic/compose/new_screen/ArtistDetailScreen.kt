package com.lalilu.lmusic.compose.new_screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import com.lalilu.lmusic.compose.component.navigate.NavigatorHeader
import com.lalilu.lmusic.utils.extension.durationToTime
import com.lalilu.lmusic.utils.extension.singleViewModel
import com.lalilu.lmusic.viewmodel.HistoryViewModel
import com.lalilu.lmusic.viewmodel.LMediaViewModel
import com.lalilu.lmusic.viewmodel.SongsViewModel

data class ArtistDetailScreen(
    private val artistName: String
) : DynamicScreen() {

    override fun getScreenInfo(): ScreenInfo = ScreenInfo(
        title = R.string.screen_title_artist_detail,
    )

    @Composable
    override fun Content() {
        ArtistDetail(artistName = artistName)
    }
}

@Composable
private fun DynamicScreen.ArtistDetail(
    artistName: String,
    mediaVM: LMediaViewModel = singleViewModel(),
    songsVM: SongsViewModel = singleViewModel(),
    historyVM: HistoryViewModel = singleViewModel(),
) {
    val artist = mediaVM.requireArtist(artistName) ?: run {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "[Error]加载失败 #$artistName")
        }
        return
    }
    val sortFor = remember { "ArtistDetail" }
    val listState = rememberLazyListState()

    val showSortPanel = remember { mutableStateOf(false) }

    val songsState by songsVM.songsState
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

    Songs(
        sortFor = sortFor,
        mediaIds = artist.songs.map { it.mediaId },
        listState = listState,
        supportGroupRules = supportGroupRules,
        supportSortRules = supportSortRules,
        supportOrderRules = supportOrderRules,
        supportSortPresets = supportSortPresets,
        headerContent = {
            item {
                NavigatorHeader(
                    title = artist.name,
                    subTitle = "共 ${songsState.values.flatten().size} 首歌曲，总时长 ${
                        artist.requireItemsDuration().durationToTime()
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
                    fontSize = 12.sp
                )
            }
        }
    )
}