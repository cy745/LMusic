package com.lalilu.lmusic.compose.screen.songs

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import com.lalilu.R
import com.lalilu.component.Songs
import com.lalilu.component.SongsScreenModel
import com.lalilu.component.base.NavigatorHeader
import com.lalilu.component.base.ScreenAction
import com.lalilu.component.base.screen.ScreenActionFactory
import com.lalilu.component.base.screen.ScreenInfo
import com.lalilu.component.base.screen.ScreenInfoFactory
import com.lalilu.component.base.screen.ScreenType
import com.lalilu.component.extension.LazyListScrollToHelper
import com.lalilu.component.extension.SelectAction
import com.lalilu.component.extension.rememberLazyListScrollToHelper
import com.lalilu.component.extension.singleViewModel
import com.lalilu.lhistory.SortRuleLastPlayTime
import com.lalilu.lhistory.SortRulePlayCount
import com.lalilu.lmedia.extension.SortStaticAction
import com.lalilu.lmusic.viewmodel.HistoryViewModel
import com.lalilu.lmusic.viewmodel.PlayingViewModel
import com.lalilu.lplaylist.PlaylistActions
import com.zhangke.krouter.annotation.Destination

@Destination("/pages/songs")
data class SubsSongsScreen(
    private val title: String? = null,
    private val mediaIds: List<String> = emptyList()
) : Screen, ScreenActionFactory, ScreenInfoFactory, ScreenType.List {

    @Transient
    private var scrollHelper: LazyListScrollToHelper? = null

    @Transient
    private var songsSM: SongsScreenModel? = null


    @Composable
    override fun provideScreenInfo(): ScreenInfo = remember {
        ScreenInfo(
            title = R.string.screen_title_songs,
            icon = R.drawable.ic_music_2_line
        )
    }

    @Composable
    override fun provideScreenActions(): List<ScreenAction> {
        val playingVM: PlayingViewModel = singleViewModel()

        return remember {
            listOf(
                ScreenAction.StaticAction(
                    title = R.string.screen_action_sort,
                    icon = R.drawable.ic_sort_desc,
                    color = Color(0xFF1793FF),
                    onAction = { songsSM?.showSortPanel?.value = true }
                ),
                ScreenAction.StaticAction(
                    title = R.string.screen_action_locate_playing_item,
                    icon = R.drawable.ic_focus_3_line,
                    color = Color(0xFF9317FF),
                    onAction = {
                        val playingId = playingVM.playing.value?.mediaId ?: return@StaticAction
                        scrollHelper?.scrollToItem(playingId)
                    }
                ),
            )
        }
    }

    @Composable
    override fun Content() {
        val listState: LazyListState = rememberLazyListState()
        val songsSM = rememberScreenModel { SongsScreenModel() }
            .also { this.songsSM = it }
        val scrollHelper = rememberLazyListScrollToHelper(listState = listState)
            .also { this.scrollHelper = it }
        val historyVM: HistoryViewModel = singleViewModel()

        Songs(
            modifier = Modifier,
            showAll = true,
            mediaIds = emptyList(),
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
                    SortStaticAction.Normal,
                    SortStaticAction.Title,
                    SortStaticAction.AddTime,
                    SortStaticAction.Duration,
                    SortRulePlayCount,
                    SortRuleLastPlayTime,
                    SortStaticAction.Shuffle
                )
            },
            showPrefixContent = { it.value == SortRulePlayCount::class.java.name },
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
