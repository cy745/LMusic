package com.lalilu.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.rememberScreenModel
import com.blankj.utilcode.util.ToastUtils
import com.lalilu.common.base.BaseSp
import com.lalilu.common.base.Playable
import com.lalilu.component.base.DynamicScreen
import com.lalilu.component.extension.DialogItem
import com.lalilu.component.extension.DialogWrapper
import com.lalilu.component.extension.ItemSelectHelper
import com.lalilu.component.extension.LazyListScrollToHelper
import com.lalilu.component.extension.SelectAction
import com.lalilu.component.extension.dayNightTextColor
import com.lalilu.component.extension.rememberItemSelectHelper
import com.lalilu.component.extension.rememberLazyListScrollToHelper
import com.lalilu.component.extension.singleViewModel
import com.lalilu.component.navigation.GlobalNavigator
import com.lalilu.component.viewmodel.IPlayingViewModel
import com.lalilu.component.viewmodel.SongsViewModel
import com.lalilu.lmedia.extension.GroupIdentity
import com.lalilu.lmedia.extension.ListAction
import com.lalilu.lmedia.extension.SortStaticAction
import com.lalilu.lmedia.extension.Sortable
import org.koin.compose.koinInject

class SongsScreenModel : ScreenModel {
    val isFastJumping = mutableStateOf(false)
    val isSelecting = mutableStateOf(false)
    val selectedItems = mutableStateOf<List<Any>>(emptyList())
    val showSortPanel = mutableStateOf(false)
}

@Composable
fun DefaultEmptyContent() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .heightIn(min = 200.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            text = stringResource(R.string.empty_screen_no_items).uppercase(),
            color = dayNightTextColor()
        )
    }
}

@Composable
fun DynamicScreen.Songs(
    modifier: Modifier = Modifier,
    mediaIds: List<String>,
    showAll: Boolean = false,
    sortFor: String = Sortable.SORT_FOR_SONGS,
    listState: LazyListState = rememberLazyListState(),
    supportListAction: () -> List<ListAction>,
    selectActions: (getAll: () -> List<Any>) -> List<SelectAction> = { emptyList() },
    scrollToHelper: LazyListScrollToHelper = rememberLazyListScrollToHelper(listState),
    songsSM: SongsScreenModel = rememberScreenModel { SongsScreenModel() },
    showPrefixContent: (sortRuleStr: State<String>) -> Boolean = { false },
    onDragMoveEnd: ((List<Playable>) -> Unit)? = null,
    emptyContent: @Composable () -> Unit = { DefaultEmptyContent() },
    prefixContent: @Composable (item: Playable, sortRuleStr: State<String>) -> Unit = { _, _ -> },
    headerContent: LazyListScope.(State<Map<GroupIdentity, List<Playable>>>) -> Unit = {},
    footerContent: LazyListScope.(State<Map<GroupIdentity, List<Playable>>>) -> Unit = {}
) {
    val navigator: GlobalNavigator = koinInject()
    val songsVM: SongsViewModel = singleViewModel()
    val playingVM: IPlayingViewModel = singleViewModel()
    val songsState = songsVM.output

    LaunchedEffect(mediaIds) {
        songsVM.updateByIds(
            songIds = mediaIds,
            sortFor = sortFor,
            showAll = showAll,
            supportSortRules = supportListAction(),
        )
    }

    val selectorHelper = rememberItemSelectHelper(
        isSelecting = songsSM.isSelecting,
        selected = songsSM.selectedItems
    )

    val sortRuleStr = registerSortPanel(
        sp = songsVM.sp,
        sortFor = sortFor,
        showPanelState = songsSM.showSortPanel,
        supportListAction = supportListAction,
    )

    registerGroupLabelJumper(
        items = { songsState.value.keys },
        scrollToHelper = scrollToHelper,
        isVisible = songsSM.isFastJumping
    )

    registerSelectPanel(
        selectActions = { selectActions { songsState.value.values.flatten() } },
        selector = selectorHelper
    )

    if (onDragMoveEnd != null) {
        ReorderableSongListWrapper(
            modifier = modifier,
            items = songsState.value.values.flatten(),
            listState = listState,
            onDragMoveEnd = onDragMoveEnd,
            scrollToHelper = { scrollToHelper },
            itemSelectHelper = { selectorHelper },
            hasLyric = { playingVM.requireHasLyric(it)[it.mediaId] ?: false },
            isFavourite = { playingVM.isFavourite(it) },
            isItemPlaying = { playingVM.isItemPlaying(it.mediaId, Playable::mediaId) },
            showPrefixContent = { showPrefixContent(sortRuleStr) },
            headerContent = { headerContent(songsState) },
            footerContent = { footerContent(songsState) },
            emptyContent = emptyContent,
            prefixContent = { prefixContent(it, sortRuleStr) },
            onLongClickItem = { navigator.goToDetailOf(it.mediaId) },
            onClickItem = {
                playingVM.play(
                    mediaId = it.mediaId,
                    mediaIds = songsState.value.values.flatten().map(Playable::mediaId),
                    playOrPause = true
                )
            },
        )
    } else {
        SongListWrapper(
            modifier = modifier,
            state = listState,
            itemsMap = songsState.value,
            idMapper = {
                when {
                    it is GroupIdentity.Time -> it.time
                    it is GroupIdentity.FirstLetter -> it.letter
                    it is GroupIdentity.DiskNumber && it.number > 0 -> it.number.toString()
                    else -> ""
                }
            },
            scrollToHelper = { scrollToHelper },
            itemSelectHelper = { selectorHelper },
            hasLyric = { playingVM.requireHasLyric(it)[it.mediaId] ?: false },
            isFavourite = { playingVM.isFavourite(it) },
            isItemPlaying = { playingVM.isItemPlaying(it.mediaId, Playable::mediaId) },
            onHeaderClick = { songsSM.isFastJumping.value = true },
            showPrefixContent = { showPrefixContent(sortRuleStr) },
            headerContent = { headerContent(songsState) },
            footerContent = { footerContent(songsState) },
            emptyContent = emptyContent,
            prefixContent = { prefixContent(it, sortRuleStr) },
            onLongClickItem = { navigator.goToDetailOf(it.mediaId) },
            onClickItem = {
                playingVM.play(
                    mediaId = it.mediaId,
                    mediaIds = songsState.value.values.flatten().map(Playable::mediaId),
                    playOrPause = true
                )
            },
        )
    }
}


@Composable
private fun registerSortPanel(
    sortFor: String,
    sp: BaseSp,
    showPanelState: MutableState<Boolean>,
    supportListAction: () -> List<ListAction>
): State<String> {
    val sortRule = sp.obtain("${sortFor}_SORT_RULE", SortStaticAction.Normal::class.java.name)
    val reverseOrder = sp.obtain("${sortFor}_SORT_RULE_REVERSE_ORDER", false)
    val flattenOverride = sp.obtain("${sortFor}_SORT_RULE_FLATTEN_OVERRIDE", false)

    val dialog = remember {
        DialogItem.Dynamic(backgroundColor = Color.Transparent) {
            SortPanel(
                sortRule = sortRule,
                reverseOrder = reverseOrder,
                flattenOverride = flattenOverride,
                supportListAction = supportListAction,
                onClose = { showPanelState.value = false }
            )
        }
    }

    DialogWrapper.register(isVisible = showPanelState, dialogItem = dialog)
    return sortRule
}


@Composable
fun DynamicScreen.registerSelectPanel(
    modifier: Modifier = Modifier,
    selector: ItemSelectHelper = rememberItemSelectHelper(),
    selectActions: () -> List<SelectAction> = { emptyList() }
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
                        LongClickableTextButton(
                            modifier = Modifier.fillMaxHeight(),
                            shape = RectangleShape,
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            colors = ButtonDefaults.textButtonColors(
                                backgroundColor = it.color.copy(alpha = 0.15f),
                                contentColor = it.color
                            ),
                            enableLongClickMask = it.forLongClick,
                            onLongClick = { if (it.forLongClick) it.onAction(selector) },
                            onClick = {
                                if (it.forLongClick) {
                                    ToastUtils.showShort("请长按此按钮以继续")
                                } else {
                                    it.onAction(selector)
                                }
                            },
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
}


@Composable
private fun registerGroupLabelJumper(
    items: () -> Collection<GroupIdentity>,
    scrollToHelper: LazyListScrollToHelper,
    isVisible: MutableState<Boolean>
) {
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues()
    val navigationBarPadding = WindowInsets.navigationBars.asPaddingValues()

    val dialog = remember {
        DialogItem.Dynamic(backgroundColor = Color.Transparent) {
            val charMapping = remember {
                items().filter { it.text.isNotBlank() }
                    .groupBy { it.text[0].category }
            }

            val paddingValues = remember {
                val topDp = statusBarPadding.calculateTopPadding()
                val bottomDp = navigationBarPadding.calculateBottomPadding()
                PaddingValues(top = topDp, bottom = bottomDp)
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = paddingValues
            ) {
                charMapping.forEach {
                    charCategoryMapping(
                        category = it.key,
                        items = it.value,
                        onClick = { key ->
                            scrollToHelper.scrollToItem(key)
                            isVisible.value = false
                        }
                    )
                }
            }
        }
    }

    DialogWrapper.register(isVisible = isVisible, dialogItem = dialog)
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterialApi::class)
private fun LazyListScope.charCategoryMapping(
    category: CharCategory,
    items: Collection<GroupIdentity>,
    onClick: (GroupIdentity) -> Unit = {}
) {
    item {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            style = MaterialTheme.typography.h6,
            color = Color.White,
            text = category.name
            // TODO 需要为CharCategory设置i18n转换
        )
    }

    item {
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.Start)
        ) {
            // TODO 需要为时间类型的分组使用日历组件，方便查找
            items.forEach { key ->
                Chip(
                    modifier = Modifier,
                    onClick = { onClick(key) }
                ) {
                    Text(
                        style = MaterialTheme.typography.h6,
                        text = key.text
                    )
                }
            }
        }
    }
}