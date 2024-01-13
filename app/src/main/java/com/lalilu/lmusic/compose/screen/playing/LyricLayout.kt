package com.lalilu.lmusic.compose.screen.playing

import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.lalilu.component.extension.rememberLazyListAnimateScroller
import com.lalilu.component.extension.rememberLazyListScrollToHelper
import com.lalilu.lmusic.utils.extension.edgeTransparent
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive

data class LyricEntry(
    val index: Int,
    val time: Long,
    val text: String,
    val translate: String? = null
) {
    val key = "$index:$time"
}

@OptIn(FlowPreview::class)
@Composable
fun LyricLayout(
    modifier: Modifier = Modifier,
    currentTime: () -> Long = { 0L },
    onItemLongClick: () -> Unit = {},
    isUserScrollEnable: () -> Boolean = { false },
    lyricEntry: State<List<LyricEntry>> = remember { mutableStateOf(emptyList()) },
    fontFamily: State<FontFamily?> = remember { mutableStateOf<FontFamily?>(null) }
) {
    val textMeasurer = rememberTextMeasurer()
    val listState = rememberLazyListState()

    val isUserTouching = remember { mutableStateOf(false) }
    val isDragged = listState.interactionSource.collectIsDraggedAsState()
    val scrollToHelper = rememberLazyListScrollToHelper(listState)
    val scroller = rememberLazyListAnimateScroller(
        listState = listState,
        enableScrollAnimation = { !isUserTouching.value },
        keysKeeper = { scrollToHelper.getKeys() }
    )

    val line: State<LyricEntry?> = remember {
        derivedStateOf {
            val time = currentTime()
            val lyricEntryList = lyricEntry.value
            if (lyricEntryList.isEmpty()) return@derivedStateOf null

            var left = 0
            var right = lyricEntryList.size
            while (left <= right) {
                val middle = (left + right) / 2
                val middleTime = lyricEntryList[middle].time
                if (time < middleTime) {
                    right = middle - 1
                } else {
                    if (middle + 1 >= lyricEntryList.size || time < lyricEntryList[middle + 1].time) {
                        return@derivedStateOf lyricEntryList[middle]
                    }
                    left = middle + 1
                }
            }
            return@derivedStateOf null
        }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { line.value }
            .collectLatest {
                it ?: return@collectLatest
                scroller.animateTo(it.key)
            }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { isDragged.value }
            .onEach { isUserTouching.value = isUserScrollEnable() }
            .debounce(5000)
            .collectLatest {
                if (!it && isActive && isUserTouching.value) {
                    isUserTouching.value = false
                    line.value?.key?.let(scroller::animateTo)
                }
            }
    }

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .edgeTransparent(top = 300.dp, bottom = 300.dp),
        userScrollEnabled = true,
        contentPadding = PaddingValues(top = 300.dp, bottom = 500.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        scrollToHelper.startRecord()

        scrollToHelper.doRecord(lyricEntry.value.map { it.key })
        items(
            items = lyricEntry.value,
            key = { it.key },
            contentType = { LyricEntry::class }
        ) {
            BoxWithConstraints(
                modifier = Modifier.padding(horizontal = 40.dp)
            ) {
                LyricSentence(
                    lyric = it,
                    constraints = constraints,
                    textMeasurer = textMeasurer,
                    fontFamily = fontFamily,
                    onLongClick = { onItemLongClick() },
                    isCurrent = { it.key == line.value?.key }
                )
            }
        }
        scrollToHelper.endRecord()
    }
}