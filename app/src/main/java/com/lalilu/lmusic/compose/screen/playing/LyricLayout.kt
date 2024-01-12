package com.lalilu.lmusic.compose.screen.playing

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lalilu.component.extension.rememberLazyListAnimateScroller
import com.lalilu.component.extension.rememberLazyListScrollToHelper
import com.lalilu.lmusic.utils.extension.edgeTransparent
import kotlinx.coroutines.flow.collectLatest

data class LyricEntry(
    val index: Int,
    val time: Long,
    val text: String,
    val translate: String? = null
) {
    val key = "$index:$time"
}

@Composable
fun LyricLayout(
    modifier: Modifier = Modifier,
    currentTime: () -> Long = { 0L },
    onItemLongClick: () -> Unit = {},
    lyricEntry: State<List<LyricEntry>> = remember { mutableStateOf(emptyList()) },
    fontFamily: State<FontFamily?> = remember { mutableStateOf<FontFamily?>(null) }
) {
    val textMeasurer = rememberTextMeasurer()
    val listState = rememberLazyListState()

    val scrollToHelper = rememberLazyListScrollToHelper(listState)
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

    val scroller = rememberLazyListAnimateScroller(
        listState = listState,
        keysKeeper = { scrollToHelper.keys }
    )

    LaunchedEffect(Unit) {
        snapshotFlow { line.value }
            .collectLatest {
                it ?: return@collectLatest
                scroller.animateTo(it.key)
            }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LyricSentence(
    lyric: LyricEntry,
    constraints: Constraints,
    textMeasurer: TextMeasurer,
    fontFamily: State<FontFamily?>,
    density: Density = LocalDensity.current,
    onLongClick: () -> Unit = {},
    onClick: () -> Unit = {},
    isCurrent: () -> Boolean
) {
    val result = remember {
        textMeasurer.measure(
            text = lyric.text,
            constraints = constraints,
            style = TextStyle.Default.copy(
                fontSize = 26.sp,
                fontFamily = fontFamily.value
                    ?: TextStyle.Default.fontFamily
            )
        )
    }

    val height = remember { result.getLineBottom(result.lineCount - 1) }
    val heightDp = remember { density.run { height.toDp() } }

    val color = animateColorAsState(
        targetValue = if (isCurrent()) Color.White else Color.Gray,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = ""
    )

    val scale = animateFloatAsState(
        targetValue = if (isCurrent()) 100f else 80f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = ""
    )

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(heightDp)
            .combinedClickable(onLongClick = onLongClick, onClick = onClick)
    ) {
        scale(
            scale = scale.value / 100f,
            pivot = Offset(x = 0f, y = height / 2f)
        ) {
            drawText(
                color = color.value,
                textLayoutResult = result,
            )
        }
    }
}