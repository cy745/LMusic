package com.lalilu.lmusic.compose.screen.playing

import android.graphics.Typeface
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lalilu.component.extension.rememberLazyListAnimateScroller
import com.lalilu.component.extension.rememberLazyListScrollToHelper
import com.lalilu.lmusic.utils.extension.edgeTransparent
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import java.io.File
import kotlin.math.abs

data class LyricEntry(
    val index: Int,
    val time: Long,
    val text: String,
    val translate: String? = null
) {
    val key = "$index:$time"
}

/**
 * 读取字体文件，并将其转换成Compose可用的FontFamily
 *
 * @param path 字体所在路径
 * @return 字体文件对应的FontFamily
 */
@Composable
fun rememberFontFamilyFromPath(path: () -> String?): State<FontFamily?> {
    val fontFamily = remember { mutableStateOf<FontFamily?>(null) }

    LaunchedEffect(path()) {
        val fontFile = path()?.takeIf { it.isNotBlank() }
            ?.let { File(it) }
            ?.takeIf { it.exists() && it.canRead() }
            ?: return@LaunchedEffect

        fontFamily.value = runCatching { FontFamily(Typeface.createFromFile(fontFile)) }
            .getOrNull()
    }

    return fontFamily
}

/**
 * 将存储的Gravity的Int值转换成Compose可用的TextAlign
 */
@Composable
fun rememberTextAlignFromGravity(gravity: () -> Int?): TextAlign {
    return remember(gravity()) {
        when (gravity()) {
            0 -> TextAlign.Start
            1 -> TextAlign.Center
            2 -> TextAlign.End
            else -> TextAlign.Start
        }
    }
}

/**
 *  将存储的Int值转换成Compose可用的TextUnit
 */
@Composable
fun rememberTextSizeFromInt(textSize: () -> Int?): TextUnit {
    return remember(textSize()) { textSize()?.takeIf { it > 0 }?.sp ?: 26.sp }
}

@OptIn(FlowPreview::class)
@Composable
fun LyricLayout(
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    currentTime: () -> Long = { 0L },
    maxWidth: () -> Int = { 1080 },
    textSize: TextUnit = 26.sp,
    textAlign: TextAlign = TextAlign.Start,
    isBlurredEnable: () -> Boolean = { false },
    isUserClickEnable: () -> Boolean = { false },
    isUserScrollEnable: () -> Boolean = { false },
    isTranslationShow: () -> Boolean = { false },
    onPositionReset: () -> Unit = {},
    onItemClick: (LyricEntry) -> Unit = {},
    onItemLongClick: (LyricEntry) -> Unit = {},
    lyricEntry: State<List<LyricEntry>> = remember { mutableStateOf(emptyList()) },
    fontFamily: State<FontFamily?> = remember { mutableStateOf<FontFamily?>(null) }
) {
    val textMeasurer = rememberTextMeasurer()
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

    // 计算当前元素在列表中的index，TODO 其实可在计算line的时候直接提取出来，待优化
    val currentItemIndex = remember {
        derivedStateOf {
            line.value?.key
                ?.let { lyricEntry.value.indexOfFirst { item -> item.key == it } }
                ?.takeIf { it >= 0 }
                ?: Int.MAX_VALUE
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
                    onPositionReset()
                }
            }
    }

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .edgeTransparent(top = 300.dp, bottom = 300.dp),
        userScrollEnabled = true,
        contentPadding = remember { PaddingValues(top = 300.dp, bottom = 500.dp) }
    ) {
        scrollToHelper.startRecord()

        scrollToHelper.doRecord(lyricEntry.value.map { it.key })
        itemsIndexed(
            items = lyricEntry.value,
            key = { _, item -> item.key },
            contentType = { _, _ -> LyricEntry::class }
        ) { index, item ->
            LyricSentence(
                lyric = item,
                maxWidth = maxWidth,
                textMeasurer = textMeasurer,
                fontFamily = fontFamily,
                textAlign = textAlign,
                textSize = textSize,
                currentTime = currentTime,
                positionToCurrent = { abs(index - currentItemIndex.value) },
                isBlurredEnable = isBlurredEnable,
                isTranslationShow = isTranslationShow,
                isCurrent = { item.key == line.value?.key },
                onLongClick = { if (isUserClickEnable()) onItemLongClick(item) },
                onClick = { if (isUserClickEnable()) onItemClick(item) }
            )
        }
        scrollToHelper.endRecord()
    }
}