package com.lalilu.lmusic.compose.screen.playing.lyric

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.funny.data_saver.core.DataSaverMutableState
import com.lalilu.component.extension.ItemRecorder
import com.lalilu.component.extension.rememberLazyListAnimateScroller
import com.lalilu.component.extension.startRecord
import com.lalilu.lmedia.lyric.LyricItem
import com.lalilu.lmedia.lyric.findPlayingIndex
import com.lalilu.lmusic.compose.screen.playing.lyric.impl.LyricContentNormal
import com.lalilu.lmusic.compose.screen.playing.lyric.impl.LyricContentWords
import com.lalilu.lmusic.utils.extension.edgeTransparent
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.isActive
import org.koin.compose.koinInject
import org.koin.core.qualifier.named


@OptIn(FlowPreview::class)
@Composable
fun LyricLayout(
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    currentTime: () -> Long = { 0L },
    lyricEntry: State<List<LyricItem>> = remember { mutableStateOf(emptyList()) },
    screenConstraints: Constraints,
    isUserClickEnable: () -> Boolean = { false },
    isUserScrollEnable: () -> Boolean = { false },
    onPositionReset: () -> Unit = {},
    onItemClick: (LyricItem) -> Unit = {},
    onItemLongClick: (LyricItem) -> Unit = {},
) {
    val settings: DataSaverMutableState<LyricSettings> = koinInject(named("LyricSettings"))
    val textMeasurer = rememberTextMeasurer()
    val isUserScrolling = remember { mutableStateOf(isUserScrollEnable()) }
        .also { it.value = isUserScrollEnable() }
    val recorder = remember { ItemRecorder() }
    val scroller = rememberLazyListAnimateScroller(
        listState = listState,
        enableScrollAnimation = { !isUserScrolling.value },
        keys = { recorder.list().filterNotNull() }
    )

    val currentItemIndex = remember {
        derivedStateOf {
            val time = currentTime() + settings.value.timeOffset
            val lyricEntryList = lyricEntry.value

            lyricEntryList.findPlayingIndex(time)
        }
    }

    val currentItem: State<LyricItem?> = remember {
        derivedStateOf {
            currentItemIndex.value
                .takeIf { it != Int.MAX_VALUE }
                ?.let { lyricEntry.value[it] }
        }
    }

    BackHandler(enabled = isUserScrolling.value) {
        isUserScrolling.value = false
        currentItem.value?.key?.let(scroller::animateTo)
        onPositionReset()
    }

    LaunchedEffect(Unit) {
        snapshotFlow { currentItem.value }
            .collectLatest {
                it ?: return@collectLatest
                scroller.animateTo(it.key)
            }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { listState.isScrollInProgress to isUserScrollEnable() }
            .debounce(5000)
            .collectLatest { pair ->
                val (isDragging, isScrolling) = pair
                if (!isActive || isDragging || !isScrolling) return@collectLatest

                isUserScrolling.value = false
                currentItem.value?.key?.let(scroller::animateTo)
                onPositionReset()
            }
    }

    val context = remember {
        LyricContext(
            currentTime = { currentTime() + settings.value.timeOffset },
            currentIndex = { currentItemIndex.value },
            isUserScrolling = { isUserScrolling.value },
            screenConstraints = screenConstraints,
            textMeasurer = textMeasurer,
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = modifier
                .fillMaxSize()
                .edgeTransparent(top = 300.dp, bottom = 250.dp),
            userScrollEnabled = true,
            contentPadding = remember { PaddingValues(top = 300.dp, bottom = 500.dp) }
        ) {
            startRecord(recorder) {
                if (lyricEntry.value.isEmpty()) {
                    itemWithRecord(key = "EMPTY_TIPS") {
                        Text("暂无歌词")
                    }
                } else {
                    itemsIndexedWithRecord(
                        items = lyricEntry.value,
                        key = { _, item -> item.key },
                        contentType = { _, _ -> LyricItem::class }
                    ) { index, item ->
                        when (item) {
                            is LyricItem.NormalLyric -> LyricContentNormal(
                                lyric = item,
                                index = index,
                                modifier = Modifier,
                                settings = settings.value,
                                context = context,
                                onLongClick = { if (isUserClickEnable()) onItemLongClick(item) },
                                onClick = { if (isUserClickEnable()) onItemClick(item) }
                            )

                            is LyricItem.WordsLyric -> LyricContentWords(
                                lyric = item,
                                index = index,
                                modifier = Modifier,
                                settings = settings.value,
                                context = context,
                                onLongClick = { if (isUserClickEnable()) onItemLongClick(item) },
                                onClick = { if (isUserClickEnable()) onItemClick(item) }
                            )
                        }
                    }
                }
            }
        }

        val contentColor = remember { Color(0xFFFFFFFF) }
        val colors = ButtonDefaults.textButtonColors(
            backgroundColor = contentColor.copy(alpha = 0.15f),
            contentColor = contentColor
        )

        AnimatedVisibility(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 72.dp)
                .fillMaxWidth(),
            enter = fadeIn() + slideIn { IntOffset(0, 100) },
            exit = fadeOut() + slideOut { IntOffset(0, 100) },
            visible = isUserScrolling.value
        ) {
            TextButton(
                modifier = Modifier.wrapContentWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = colors,
                onClick = {
                    isUserScrolling.value = false
                    currentItem.value?.key?.let(scroller::animateTo)
                    onPositionReset()
                }
            ) {
                Text(
                    text = "退出歌词滚动模式",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}