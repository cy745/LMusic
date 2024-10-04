package com.lalilu.lmusic.compose.screen.playing

import android.graphics.Typeface
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lalilu.component.extension.ItemRecorder
import com.lalilu.component.extension.rememberLazyListAnimateScroller
import com.lalilu.component.extension.startRecord
import com.lalilu.lmedia2.lyric.LyricItem
import com.lalilu.lmedia2.lyric.LyricUtils
import com.lalilu.lmusic.utils.extension.edgeTransparent
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.isActive
import java.io.File
import java.util.WeakHashMap
import kotlin.math.abs


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

private val EMPTY_SENTENCE_TIPS = LyricItem.SingleLyric(
    time = 0,
    content = "暂无歌词",
)

private val indexKeeper = WeakHashMap<LyricItem, Int>()
var LyricItem.index: Int
    get() = indexKeeper[this] ?: -1
    set(value) = run { indexKeeper[this] = value }

val LyricItem.key
    get() = "${index}:$time"

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
    onItemClick: (LyricItem) -> Unit = {},
    onItemLongClick: (LyricItem) -> Unit = {},
    lyricEntry: State<List<LyricItem>> = remember { mutableStateOf(emptyList()) },
    fontFamily: State<FontFamily?> = remember { mutableStateOf<FontFamily?>(null) }
) {
    val textMeasurer = rememberTextMeasurer()
    val isUserScrolling = remember(isUserScrollEnable()) { mutableStateOf(isUserScrollEnable()) }
    val recorder = remember { ItemRecorder() }
    val scroller = rememberLazyListAnimateScroller(
        listState = listState,
        enableScrollAnimation = { !isUserScrolling.value },
        keys = { recorder.list().filterNotNull() }
    )

    val currentItemIndex = remember {
        derivedStateOf {
            val time = currentTime()
            val lyricEntryList = lyricEntry.value

            LyricUtils.findPlayingIndex(time, lyricEntryList)
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
                        LyricSentence(
                            lyric = EMPTY_SENTENCE_TIPS,
                            maxWidth = maxWidth,
                            textMeasurer = textMeasurer,
                            fontFamily = fontFamily,
                            textAlign = textAlign,
                            textSize = textSize,
                            currentTime = currentTime,
                            isBlurredEnable = isBlurredEnable,
                            isTranslationShow = isTranslationShow,
                            isCurrent = { true },
                            onLongClick = {
                                if (isUserClickEnable()) onItemLongClick(
                                    EMPTY_SENTENCE_TIPS
                                )
                            }
                        )
                    }
                } else {
                    itemsIndexedWithRecord(
                        items = lyricEntry.value,
                        key = { _, item -> item.key },
                        contentType = { _, _ -> LyricItem::class }
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
                            isCurrent = { item.key == currentItem.value?.key },
                            onLongClick = { if (isUserClickEnable()) onItemLongClick(item) },
                            onClick = { if (isUserClickEnable()) onItemClick(item) }
                        )
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