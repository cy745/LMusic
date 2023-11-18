package com.lalilu.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Chip
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.rememberScreenModel
import com.lalilu.common.base.BaseSp
import com.lalilu.common.base.Playable
import com.lalilu.component.base.DynamicScreen
import com.lalilu.component.extension.ItemSelectHelper
import com.lalilu.component.extension.LazyListScrollToHelper
import com.lalilu.component.extension.SelectAction
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
    selectActions: () -> List<SelectAction> = { emptyList() },
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
            selectActions = selectActions,
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
    modifier: Modifier = Modifier,
    selector: ItemSelectHelper = rememberItemSelectHelper(),
    selectActions: () -> List<SelectAction> = { emptyList() },
    content: @Composable (selector: ItemSelectHelper) -> Unit,
) {
    RegisterMainContent(
        isVisible = selector.isSelecting,
        onBackPressed = { selector.clear() }
    ) {
        Row(
            modifier = modifier
                .clickable(enabled = false) {}
                .height(52.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(
                modifier = Modifier.fillMaxHeight(),
                shape = RectangleShape,
                contentPadding = PaddingValues(start = 16.dp, end = 24.dp),
                colors = ButtonDefaults.textButtonColors(
                    backgroundColor = Color(0x2F006E7C),
                    contentColor = Color(0xFF006E7C)
                ),
                onClick = { selector.clear() }
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_close_line),
                    contentDescription = "cancelButton",
                    colorFilter = ColorFilter.tint(color = Color(0xFF006E7C))
                )
                Text(
                    text = "取消 [${selector.selected.value.size}]",
                    fontSize = 14.sp
                )
            }

            LazyRow(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.End
            ) {
                items(items = selectActions()) {
                    if (it is SelectAction.ComposeAction) {
                        it.content.invoke(selector)
                        return@items
                    }

                    if (it is SelectAction.StaticAction) {
                        TextButton(
                            modifier = Modifier.fillMaxHeight(),
                            shape = RectangleShape,
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            colors = ButtonDefaults.textButtonColors(
                                backgroundColor = it.color.copy(alpha = 0.15f),
                                contentColor = it.color
                            ),
                            onClick = { it.onAction(selector) }
                        ) {
                            it.icon?.let { icon ->
                                Image(
                                    modifier = Modifier.size(20.dp),
                                    painter = painterResource(id = icon),
                                    contentDescription = stringResource(id = it.title),
                                    colorFilter = ColorFilter.tint(color = it.color)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text(
                                text = stringResource(id = it.title),
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
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