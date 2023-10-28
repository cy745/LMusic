package com.lalilu.lmusic.compose.new_screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Chip
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lalilu.R
import com.lalilu.common.base.Playable
import com.lalilu.lmedia.entity.LSong
import com.lalilu.lmedia.extension.GroupIdentity
import com.lalilu.lmedia.extension.GroupRule
import com.lalilu.lmedia.extension.OrderRule
import com.lalilu.lmedia.extension.SortRule
import com.lalilu.lmedia.extension.Sortable
import com.lalilu.lmusic.compose.DynamicScreen
import com.lalilu.lmusic.compose.NavigationWrapper
import com.lalilu.lmusic.compose.ScreenAction
import com.lalilu.lmusic.compose.ScreenInfo
import com.lalilu.lmusic.compose.component.ItemSelectHelper
import com.lalilu.lmusic.compose.component.LLazyColumn
import com.lalilu.lmusic.compose.component.LazyListScrollToHelper
import com.lalilu.lmusic.compose.component.base.IconTextButton
import com.lalilu.lmusic.compose.component.base.SortPanel
import com.lalilu.lmusic.compose.component.base.SortPreset
import com.lalilu.lmusic.compose.component.card.SongCard
import com.lalilu.lmusic.compose.component.navigate.NavigatorHeader
import com.lalilu.lmusic.compose.component.rememberItemSelectHelper
import com.lalilu.lmusic.compose.component.rememberLazyListScrollToHelper
import com.lalilu.lmusic.compose.component.rememberStickyHelper
import com.lalilu.lmusic.compose.component.stickyHeaderExtent
import com.lalilu.lmusic.datastore.SettingsSp
import com.lalilu.lmusic.utils.extension.rememberFixedStatusBarHeightDp
import com.lalilu.lmusic.utils.extension.singleViewModel
import com.lalilu.lmusic.viewmodel.HistoryViewModel
import com.lalilu.lmusic.viewmodel.PlayingViewModel
import com.lalilu.lmusic.viewmodel.SongsViewModel
import com.lalilu.lmusic.viewmodel.TempViewModel
import org.koin.compose.koinInject

private val defaultSortPresets = listOf(
    SortPreset.SortByAddTime,
    SortPreset.SortByTitle,
    SortPreset.SortByLastPlayTime,
    SortPreset.SortByPlayedTimes,
    SortPreset.SortByDuration
)
private val defaultSortRules = listOf(
    SortRule.Normal,
    SortRule.Title,
    SortRule.CreateTime,
    SortRule.ModifyTime,
    SortRule.ItemsDuration,
    SortRule.PlayCount,
    SortRule.LastPlayTime
)
private val defaultGroupRules = listOf(
    GroupRule.Normal,
    GroupRule.CreateTime,
    GroupRule.ModifyTime,
    GroupRule.PinYinFirstLetter,
    GroupRule.TitleFirstLetter
)
private val defaultOrderRules = listOf(
    OrderRule.Normal,
    OrderRule.Reverse,
    OrderRule.Shuffle
)

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
        val tempVM: TempViewModel = singleViewModel()
        val playingVM: PlayingViewModel = singleViewModel()
        val historyVM: HistoryViewModel = singleViewModel()

        val listState: LazyListState = rememberLazyListState()
        val scrollHelper = rememberLazyListScrollToHelper(listState = listState)

        RegisterActions {
            listOf(
                ScreenAction.StaticAction(
                    title = R.string.screen_action_sort,
                    icon = R.drawable.ic_sort_desc,
                    color = Color(0xFF1793FF),
                    onAction = { tempVM.showSortPanel.value = true }
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
            mediaIds = mediaIds,
            listState = listState,
            scrollToHelper = scrollHelper,
            showPrefixContent = { sortRuleStr ->
                sortRuleStr.value == SortRule.TrackNumber.name || sortRuleStr.value == SortRule.PlayCount.name
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
}

@Composable
fun DynamicScreen.Songs(
    mediaIds: List<String>,
    sortFor: String = Sortable.SORT_FOR_SONGS,
    supportSortPresets: List<SortPreset> = defaultSortPresets,
    supportGroupRules: List<GroupRule> = defaultGroupRules,
    supportSortRules: List<SortRule> = defaultSortRules,
    supportOrderRules: List<OrderRule> = defaultOrderRules,
    listState: LazyListState = rememberLazyListState(),
    scrollToHelper: LazyListScrollToHelper = rememberLazyListScrollToHelper(listState),
    songsVM: SongsViewModel = singleViewModel(),
    playingVM: PlayingViewModel = singleViewModel(),
    tempVM: TempViewModel = singleViewModel(),
    showPrefixContent: (sortRuleStr: State<String>) -> Boolean = { false },
    prefixContent: @Composable (item: LSong, sortRuleStr: State<String>) -> Unit = { _, _ -> },
    headerContent: LazyListScope.(State<Map<GroupIdentity, List<LSong>>>) -> Unit = {}
) {
    LaunchedEffect(mediaIds) {
        songsVM.updateByIds(
            songIds = mediaIds,
            sortFor = sortFor,
            supportSortRules = supportSortRules,
            supportOrderRules = supportOrderRules,
            supportGroupRules = supportGroupRules,
        )
    }

    val songsState by songsVM.songsState

    HeaderJumperWrapper(
        items = { songsState.keys },
        isVisible = tempVM.isFastJumping,
        scrollToHelper = scrollToHelper
    ) { scrollHelper, headerJumperWrapperVisible ->
        SelectPanelWrapper(
            selector = rememberItemSelectHelper(
                isSelecting = tempVM.isSelecting,
                selected = tempVM.selectedItems
            )
        ) { selector ->
            SortPanelWrapper(
                sortFor = sortFor,
                showPanelState = tempVM.showSortPanel,
                supportSortPresets = { supportSortPresets },
                supportGroupRules = { supportGroupRules },
                supportOrderRules = { supportOrderRules },
                supportSortRules = { supportSortRules }
            ) { sortRuleStr ->
                SongListWrapper(
                    state = listState,
                    itemsMap = songsState,
                    scrollToHelper = { scrollHelper },
                    itemSelectHelper = { selector },
                    hasLyricState = { playingVM.requireHasLyricState(item = it) },
                    isItemPlaying = { playingVM.isItemPlaying(it.id, Playable::mediaId) },
                    onHeaderClick = { headerJumperWrapperVisible.value = true },
                    showPrefixContent = { showPrefixContent(sortRuleStr) },
                    headerContent = { headerContent(songsVM.songsState) },
                    prefixContent = { prefixContent(it, sortRuleStr) },
                    onClickItem = {
                        playingVM.play(
                            mediaId = it.mediaId,
                            mediaIds = songsState.values.flatten().map(Playable::mediaId),
                            playOrPause = true
                        )
                    },
                    onLongClickItem = {
                        NavigationWrapper.navigator?.showSingle(SongDetailScreen(mediaId = it.id))
                    },
                )
            }
        }
    }
}

@Composable
fun DynamicScreen.SortPanelWrapper(
    sortFor: String,
    settingsSp: SettingsSp = koinInject(),
    showPanelState: MutableState<Boolean>,
    supportSortPresets: () -> List<SortPreset>,
    supportGroupRules: () -> List<GroupRule>,
    supportSortRules: () -> List<SortRule>,
    supportOrderRules: () -> List<OrderRule>,
    content: @Composable (sortRuleStr: State<String>) -> Unit,
) {
    val sortRule = settingsSp.stringSp("${sortFor}_SORT_RULE", SortRule.Normal.name)
    val orderRule = settingsSp.stringSp("${sortFor}_ORDER_RULE", OrderRule.Normal.name)
    val groupRule = settingsSp.stringSp("${sortFor}_GROUP_RULE", GroupRule.Normal.name)

    RegisterMainContent(
        showMask = { true },
        showBackground = { false },
        isVisible = showPanelState
    ) {
        SortPanel(
            sortRule = sortRule,
            orderRule = orderRule,
            groupRule = groupRule,
            supportSortPresets = supportSortPresets,
            supportGroupRules = supportGroupRules,
            supportOrderRules = supportOrderRules,
            supportSortRules = supportSortRules,
            onClose = { showPanelState.value = false }
        )
    }
    content(sortRule)
}

@Composable
private fun DynamicScreen.SelectPanelWrapper(
    selector: ItemSelectHelper = rememberItemSelectHelper(),
    content: @Composable (selector: ItemSelectHelper) -> Unit,
) {
    RegisterMainContent(
        isVisible = selector.isSelecting,
        onBackPressed = { selector.clear() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            IconTextButton(
                text = "取消",
                color = Color(0xFF006E7C),
                onClick = { selector.clear() }
            )
            Text(text = "已选择: ${selector.selected.value.size}")
        }
    }

    content(selector)
}


@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterialApi::class)
@Composable
private fun DynamicScreen.HeaderJumperWrapper(
    items: () -> Collection<GroupIdentity>,
    scrollToHelper: LazyListScrollToHelper,
    isVisible: MutableState<Boolean>,
    content: @Composable (LazyListScrollToHelper, MutableState<Boolean>) -> Unit,
) {
    RegisterMainContent(
        isVisible = isVisible,
        showMask = { true },
        showBackground = { false }
    ) {
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(state = rememberScrollState())
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally)
        ) {
            items().forEach { key ->
                Chip(
                    modifier = Modifier,
                    onClick = {
                        scrollToHelper.scrollToItem(key)
                        isVisible.value = false
                    }
                ) {
                    Text(
                        style = MaterialTheme.typography.h6,
                        text = when (key) {
                            is GroupIdentity.Time -> key.time
                            is GroupIdentity.DiskNumber -> key.number.toString()
                            is GroupIdentity.FirstLetter -> key.letter
                            else -> ""
                        }
                    )
                }
            }
        }
    }
    content(scrollToHelper, isVisible)
}


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
private fun SongListWrapper(
    state: LazyListState = rememberLazyListState(),
    itemSelectHelper: () -> ItemSelectHelper? = { null },
    scrollToHelper: () -> LazyListScrollToHelper? = { null },
    itemsMap: Map<GroupIdentity, List<LSong>>,
    hasLyricState: (LSong) -> State<Boolean>,
    onClickItem: (LSong) -> Unit = {},
    onLongClickItem: (LSong) -> Unit = {},
    onHeaderClick: (Any) -> Unit = {},
    isItemPlaying: (LSong) -> Boolean = { false },
    showPrefixContent: () -> Boolean = { false },
    prefixContent: @Composable (item: LSong) -> Unit = {},
    headerContent: LazyListScope.() -> Unit,
) {
    val haptic = LocalHapticFeedback.current

    val scrollHelper = remember { scrollToHelper() }
    val selector = remember { itemSelectHelper() }
    val stickyHelper = rememberStickyHelper(
        listState = state,
        contentType = { GroupIdentity::class }
    )

    LLazyColumn(
        modifier = Modifier,
        state = state,
        contentPadding = PaddingValues(top = rememberFixedStatusBarHeightDp()),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        headerContent()
        scrollHelper?.startRecord()

        for ((key, list) in itemsMap) {
            var skip = key == GroupIdentity.None
            skip = skip || (key is GroupIdentity.DiskNumber && key.number < 0)

            if (!skip) {
                scrollHelper?.doRecord(key)
                stickyHeaderExtent(
                    helper = stickyHelper,
                    key = { key }
                ) {
                    Chip(
                        modifier = Modifier
                            .animateItemPlacement()
                            .offsetWithHelper()
                            .zIndexWithHelper(),
                        onClick = { onHeaderClick(key) }
                    ) {
                        Text(
                            style = MaterialTheme.typography.h6,
                            text = when (key) {
                                is GroupIdentity.Time -> key.time
                                is GroupIdentity.DiskNumber -> key.number.toString()
                                is GroupIdentity.FirstLetter -> key.letter
                                else -> ""
                            }
                        )
                    }
                }
            }

            scrollHelper?.doRecord(list.map { it.id })
            items(
                items = list,
                key = { it.id },
                contentType = { LSong::class }
            ) { item ->
                SongCard(
                    song = { item },
                    modifier = Modifier.animateItemPlacement(),
                    hasLyric = hasLyricState(item),
                    onClick = {
                        if (selector?.isSelecting() == true) {
                            selector.onSelect(item)
                        } else {
                            onClickItem(item)
                        }
                    },
                    onLongClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onLongClickItem(item)
                    },
                    onEnterSelect = { selector?.onSelect(item) },
                    isSelected = { selector?.isSelected(item) ?: false },
                    isPlaying = { isItemPlaying(item) },
                    showPrefix = showPrefixContent,
                    prefixContent = { modifier ->
                        Row(
                            modifier = modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colors.surface)
                                .padding(start = 4.dp, end = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            prefixContent(item)
                        }
                    }
                )
            }
        }
        scrollHelper?.endRecord()
    }
}