package com.lalilu.lmusic.compose.new_screen

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.rememberScreenModel
import com.lalilu.R
import com.lalilu.component.Songs
import com.lalilu.component.SongsScreenModel
import com.lalilu.component.base.DynamicScreen
import com.lalilu.component.base.ScreenAction
import com.lalilu.component.base.ScreenInfo
import com.lalilu.component.extension.SelectAction
import com.lalilu.component.extension.rememberLazyListScrollToHelper
import com.lalilu.component.extension.singleViewModel
import com.lalilu.lmedia.extension.GroupRuleStatic
import com.lalilu.lmedia.extension.ListActionPreset
import com.lalilu.lmedia.extension.OrderRuleStatic
import com.lalilu.lmedia.extension.SortRuleStatic
import com.lalilu.lmusic.compose.component.navigate.NavigatorHeader
import com.lalilu.lhistory.SortRuleLastPlayTime
import com.lalilu.lhistory.SortRulePlayCount
import com.lalilu.lhistory.SortRulePresetLastPlayTime
import com.lalilu.lhistory.SortRulePresetPlayedTimes
import com.lalilu.lmusic.viewmodel.HistoryViewModel
import com.lalilu.lmusic.viewmodel.PlayingViewModel
import com.lalilu.lplaylist.PlaylistActions

data class SongsScreen(
    private val title: String? = null,
    private val mediaIds: List<String> = emptyList()
) : DynamicScreen() {

    override fun getScreenInfo(): ScreenInfo = ScreenInfo(
        title = R.string.screen_title_songs,
        icon = R.drawable.ic_music_2_line
    )

    @Composable
    override fun Content() {
        val playingVM: PlayingViewModel = singleViewModel()
        val historyVM: HistoryViewModel = singleViewModel()
        val songsSM: SongsScreenModel = rememberScreenModel { SongsScreenModel() }

        val listState: LazyListState = rememberLazyListState()
        val scrollHelper = rememberLazyListScrollToHelper(listState = listState)

        RegisterActions {
            listOf(
                ScreenAction.StaticAction(
                    title = R.string.screen_action_sort,
                    icon = R.drawable.ic_sort_desc,
                    color = Color(0xFF1793FF),
                    onAction = { songsSM.showSortPanel.value = true }
                ),
                ScreenAction.StaticAction(
                    title = R.string.screen_action_locate_playing_item,
                    icon = R.drawable.ic_focus_3_line,
                    color = Color(0xFF9317FF),
                    onAction = {
                        val playingId = playingVM.playing.value?.mediaId ?: return@StaticAction
                        scrollHelper.scrollToItem(playingId)
                    }
                ),
            )
        }

        Songs(
            showAll = true,
            mediaIds = mediaIds,
            listState = listState,
            songsSM = songsSM,
            scrollToHelper = scrollHelper,
            selectActions = { getAll ->
                listOf(
                    SelectAction.StaticAction.SelectAll(getAll = getAll),
                    SelectAction.StaticAction.ClearAll,
                    PlaylistActions.addToPlaylistAction,
                    PlaylistActions.addToFavorite,
                )
            },
            supportListAction = {
                listOf(
                    // 部分预设
                    ListActionPreset.SortByAddTime,
                    ListActionPreset.SortByModifyTime,
                    ListActionPreset.SortByTitle,
                    ListActionPreset.SortByDuration,
                    SortRulePresetLastPlayTime,
                    SortRulePresetPlayedTimes,

                    // SortRule
                    SortRuleStatic.Normal,
                    SortRuleStatic.CreateTime,
                    SortRuleStatic.Title,
                    SortRuleStatic.SubTitle,
                    SortRuleStatic.ItemsDuration,
                    SortRuleStatic.ContentType,
                    SortRuleStatic.ModifyTime,
                    SortRulePlayCount,
                    SortRuleLastPlayTime,

                    // GroupRule
                    GroupRuleStatic.Normal,
                    GroupRuleStatic.CreateTime,
                    GroupRuleStatic.ModifyTime,
                    GroupRuleStatic.TitleFirstLetter,
                    GroupRuleStatic.SubTitleFirstLetter,
                    GroupRuleStatic.PinYinFirstLetter,

                    // OrderRule
                    OrderRuleStatic.Normal,
                    OrderRuleStatic.Reverse,
                    OrderRuleStatic.Shuffle
                )
            },
            showPrefixContent = {
                it.value == SortRuleStatic.TrackNumber::class.java.name ||
                        it.value == SortRulePlayCount::class.java.name
            },
            headerContent = {
                item {
                    NavigatorHeader(
                        title = title ?: "全部歌曲",
                        subTitle = "共 ${it.value.values.flatten().size} 首歌曲"
                    )
                }
            },
            prefixContent = { item, sortRuleStr ->
                var icon = -1
                var text = ""
                when (sortRuleStr.value) {
                    SortRulePlayCount::class.java.name -> {
                        icon = R.drawable.headphone_fill
                        text = historyVM.requiteHistoryCountById(item.mediaId).toString()
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
}