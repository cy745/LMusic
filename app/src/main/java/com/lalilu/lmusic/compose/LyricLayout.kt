package com.lalilu.lmusic.compose

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.collectIsDraggedAsState
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
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
import androidx.dynamicanimation.animation.SpringForce
import androidx.dynamicanimation.animation.springAnimationOf
import androidx.dynamicanimation.animation.withSpringForceProperties
import com.dirror.lyricviewx.LyricEntry
import com.lalilu.component.extension.rememberLazyListScrollToHelper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.isActive
import kotlin.random.Random

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
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
    val time by rememberUpdatedState(newValue = currentTime())
    val line: State<LyricEntry?> = remember {
        derivedStateOf {
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

    val isDragged = listState.interactionSource.collectIsDraggedAsState()

    val currentValue = remember { mutableFloatStateOf(0f) }
    val targetValue = remember { mutableFloatStateOf(0f) }
    val deltaValue = remember { mutableFloatStateOf(0f) }
    val targetRange = remember { mutableStateOf(IntRange(0, 0)) }
    val sizeMap = remember { mutableStateMapOf<Int, Int>() }

    val animator = remember {
        springAnimationOf(
            getter = { currentValue.floatValue },
            setter = {
                deltaValue.floatValue = it - currentValue.floatValue
                currentValue.floatValue = it
            },
            finalPosition = 0f
        ).withSpringForceProperties {
            dampingRatio = SpringForce.DAMPING_RATIO_NO_BOUNCY
            stiffness = SpringForce.STIFFNESS_VERY_LOW
        }.addEndListener { animation, canceled, value, velocity ->
            if (!canceled) {
                targetRange.value = IntRange.EMPTY
            }
        }
    }

    LaunchedEffect(targetValue.floatValue) {
        animator.animateToFinalPosition(targetValue.floatValue)
    }

    LaunchedEffect(deltaValue.floatValue) {
        listState.scroll { scrollBy(deltaValue.floatValue) }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo }
            .distinctUntilChanged()
            .collectLatest { list ->
                list.forEach {
                    // 若当前更新的元素处于动画偏移值计算数据源的目标范围中
                    if (it.index in targetRange.value && animator.isRunning) {
                        val delta = it.size - (sizeMap[it.index] ?: 0)

                        if (delta != 0) {
                            // 则将变化值直接更新到targetValue，触发动画位移修正
                            println("update targetValue:  [${it.index} -> $delta]")
                            targetValue.floatValue += delta
                        }
                    }
                    sizeMap[it.index] = it.size
                }
            }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { line.value }
            .distinctUntilChanged()
            .mapLatest {
                // 1. 从当前可见元素直接查找offset （准确值）
                // get the offset directly from the visibleItemsInfo
                val offset = listState.layoutInfo.visibleItemsInfo
                    .firstOrNull { it.key == line.value?.time }
                    ?.offset

                if (offset != null) {
                    animator.cancel()
                    currentValue.floatValue = 0f
                    targetValue.floatValue =
                        offset.toFloat() + Random(System.currentTimeMillis()).nextFloat() * 0.1f
                    // 添加随机值，为了确保能触发LaunchedEffect重组

                    println("[visible target]: ${targetValue.floatValue}")
                    return@mapLatest offset
                }

                return@mapLatest null
            }
            .debounce(20L)
            .collectLatest { result ->
                if (result != null) return@collectLatest

                // 2. 使用实时维护的sizeMap查找并计算目标元素的offset （非准确值）
                // Use the real-time maintained sizeMap to find and calculate the offset of the target element
                val index = scrollToHelper.keys.indexOfFirst { it == line.value?.time }
                if (index == -1) return@collectLatest // 元素不存在keys列表中，则不进行滚动

                if (!isActive) return@collectLatest
                val firstVisibleIndex = listState.firstVisibleItemIndex
                val firstVisibleOffset = listState.firstVisibleItemScrollOffset
                targetRange.value = minOf(firstVisibleIndex, index)..maxOf(firstVisibleIndex, index)

                // 计算方向乘数，向下滚动则为正数
                // calculate the direction multiplier, if scrolling down, it's positive
                val forwardMultiple = if (index >= firstVisibleIndex) 1f else -1f

                if (!isActive) return@collectLatest
                // 计算目标距离，若未缓存有相应位置的值，则计算使用平均值
                // calculate the target offset，if these no value cached then use the average value
                val sizeAverage = sizeMap.values.average().toInt()
                val sizeSum = targetRange.value.sumOf { sizeMap.getOrPut(it) { sizeAverage } }
                val spacingSum =
                    (targetRange.value.last - targetRange.value.first) * listState.layoutInfo.mainAxisItemSpacing
                var offsetTemp = (sizeSum + spacingSum) * forwardMultiple

                val firstVisibleHeight = listState.layoutInfo.visibleItemsInfo
                    .getOrNull(firstVisibleIndex)?.size
                    ?: sizeMap[firstVisibleIndex]
                    ?: sizeAverage

                // 针对firstVisibleItem的边界情况修正offset值
                // fix the offset value for the boundary case of the firstVisibleItem
                offsetTemp += if (forwardMultiple > 0) firstVisibleOffset else (firstVisibleOffset - firstVisibleHeight)

                // 使用非准确值进行滚动
                // use the non-accurate value for scrolling
                if (!isActive) return@collectLatest
                animator.cancel()
                currentValue.floatValue = 0f
                targetValue.floatValue =
                    offsetTemp + Random(System.currentTimeMillis()).nextFloat() * 0.1f

                println("[calculate target]: ${targetValue.floatValue} -> range: [${targetRange.value.first} -> ${targetRange.value.last}]")
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

        scrollToHelper.doRecord(lyricEntry.value.map { it.time })
        items(
            items = lyricEntry.value,
            key = { it.time },
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
                    isCurrent = { it.time == line.value?.time }
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