package com.lalilu.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Chip
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.rememberScreenModel
import com.lalilu.common.base.BaseSp
import com.lalilu.common.base.Playable
import com.lalilu.component.base.DynamicScreen
import com.lalilu.component.extension.ItemSelectHelper
import com.lalilu.component.extension.LazyListScrollToHelper
import com.lalilu.component.extension.rememberItemSelectHelper
import com.lalilu.component.extension.rememberLazyListScrollToHelper
import com.lalilu.component.extension.singleViewModel
import com.lalilu.component.navigation.GlobalNavigator
import com.lalilu.component.viewmodel.IPlayingViewModel
import com.lalilu.component.viewmodel.SongsViewModel
import com.lalilu.lmedia.extension.GroupIdentity
import com.lalilu.lmedia.extension.GroupRuleStatic
import com.lalilu.lmedia.extension.ListAction
import com.lalilu.lmedia.extension.OrderRuleStatic
import com.lalilu.lmedia.extension.SortRuleStatic
import com.lalilu.lmedia.extension.Sortable
import org.koin.compose.koinInject

class SongsScreenModel : ScreenModel {
    val isFastJumping = mutableStateOf(false)
    val isSelecting = mutableStateOf(false)
    val selectedItems = mutableStateOf<List<Any>>(emptyList())
    val showSortPanel = mutableStateOf(false)
}

@Composable
fun DynamicScreen.Songs(
    mediaIds: List<String>,
    showAll: Boolean = false,
    sortFor: String = Sortable.SORT_FOR_SONGS,
    listState: LazyListState = rememberLazyListState(),
    supportListAction: () -> List<ListAction>,
    scrollToHelper: LazyListScrollToHelper = rememberLazyListScrollToHelper(listState),
    songsSM: SongsScreenModel = rememberScreenModel { SongsScreenModel() },
    showPrefixContent: (sortRuleStr: State<String>) -> Boolean = { it.value == SortRuleStatic.TrackNumber::class.java.name },
    prefixContent: @Composable (item: Playable, sortRuleStr: State<String>) -> Unit = { _, _ -> },
    headerContent: LazyListScope.(State<Map<GroupIdentity, List<Playable>>>) -> Unit = {},
    footerContent: LazyListScope.(State<Map<GroupIdentity, List<Playable>>>) -> Unit = {}
) {
    val navigator: GlobalNavigator = koinInject()
    val songsVM: SongsViewModel = singleViewModel()
    val playingVM: IPlayingViewModel = singleViewModel()
    val songsState by songsVM.output

    LaunchedEffect(mediaIds) {
        songsVM.updateByIds(
            songIds = mediaIds,
            sortFor = sortFor,
            showAll = showAll,
            supportSortRules = supportListAction(),
        )
    }

    HeaderJumperWrapper(
        items = { songsState.keys },
        isVisible = songsSM.isFastJumping,
        scrollToHelper = scrollToHelper
    ) { scrollHelper, headerJumperWrapperVisible ->
        SelectPanelWrapper(
            selector = rememberItemSelectHelper(
                isSelecting = songsSM.isSelecting,
                selected = songsSM.selectedItems
            )
        ) { selector ->
            SortPanelWrapper(
                sp = songsVM.sp,
                sortFor = sortFor,
                showPanelState = songsSM.showSortPanel,
                supportListAction = supportListAction,
            ) { sortRuleStr ->
                SongListWrapper(
                    state = listState,
                    itemsMap = songsState,
                    idMapper = {
                        when {
                            it is GroupIdentity.Time -> it.time
                            it is GroupIdentity.FirstLetter -> it.letter
                            it is GroupIdentity.DiskNumber && it.number > 0 -> it.number.toString()
                            else -> ""
                        }
                    },
                    scrollToHelper = { scrollHelper },
                    itemSelectHelper = { selector },
                    hasLyric = { playingVM.requireHasLyric(it)[it.mediaId] ?: false },
                    isItemPlaying = { playingVM.isItemPlaying(it.mediaId, Playable::mediaId) },
                    onHeaderClick = { headerJumperWrapperVisible.value = true },
                    showPrefixContent = { showPrefixContent(sortRuleStr) },
                    headerContent = { headerContent(songsVM.output) },
                    footerContent = { footerContent(songsVM.output) },
                    prefixContent = { prefixContent(it, sortRuleStr) },
                    onLongClickItem = { navigator.goToDetailOf(it.mediaId) },
                    onClickItem = {
                        playingVM.play(
                            mediaId = it.mediaId,
                            mediaIds = songsState.values.flatten().map(Playable::mediaId),
                            playOrPause = true
                        )
                    },
                )
            }
        }
    }
}


@Composable
fun DynamicScreen.SortPanelWrapper(
    sortFor: String,
    sp: BaseSp,
    showPanelState: MutableState<Boolean>,
    supportListAction: () -> List<ListAction>,
    content: @Composable (sortRuleStr: State<String>) -> Unit,
) {
    val sortRule = sp.obtain("${sortFor}_SORT_RULE", SortRuleStatic.Normal::class.java.name)
    val orderRule = sp.obtain("${sortFor}_ORDER_RULE", OrderRuleStatic.Normal::class.java.name)
    val groupRule = sp.obtain("${sortFor}_GROUP_RULE", GroupRuleStatic.Normal::class.java.name)

    RegisterMainContent(
        showMask = { true },
        showBackground = { false },
        isVisible = showPanelState
    ) {
        SortPanel(
            sortRule = sortRule,
            orderRule = orderRule,
            groupRule = groupRule,
            supportListAction = supportListAction,
            onClose = { showPanelState.value = false }
        )
    }
    content(sortRule)
}


@Composable
fun DynamicScreen.SelectPanelWrapper(
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