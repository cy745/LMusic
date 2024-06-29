package com.lalilu.component.extension

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import androidx.dynamicanimation.animation.springAnimationOf
import androidx.dynamicanimation.animation.withSpringForceProperties
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

class LazyListAnimateScroller internal constructor(
    private val keysKeeper: () -> Collection<Any>,
    private val listState: LazyListState,
    private val currentValue: MutableFloatState,
    private val targetValue: MutableFloatState,
    private val deltaValue: MutableFloatState,
    private val targetRange: MutableState<IntRange>,
    private val sizeMap: SnapshotStateMap<Int, Int>
) {
    private val keyEvent: MutableSharedFlow<Any> = MutableSharedFlow(1)
    private var exactAnimation: Boolean = false
    private val animator: SpringAnimation = springAnimationOf(
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

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    internal suspend fun startLoop(scope: CoroutineScope) = withContext(scope.coroutineContext) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo }
            .distinctUntilChanged()
            .onEach { list -> list.forEach { sizeMap[it.index] = it.size } }
            .launchIn(this)

        keyEvent.mapLatest { key ->
            // 1. 从当前可见元素直接查找offset （准确值）
            // get the offset directly from the visibleItemsInfo
            val offset = listState.layoutInfo.visibleItemsInfo
                .firstOrNull { it.key == key }
                ?.offset

            if (offset != null) {
                doScroll(offset.toFloat(), true)
                println("[visible target]: ${targetValue.floatValue}")
                return@mapLatest null
            }

            return@mapLatest key
        }.debounce(20L)
            .collectLatest { key ->
                if (key == null) return@collectLatest

                val index = keysKeeper().indexOfFirst { it == key }
                if (index == -1) return@collectLatest // 元素不存在keys列表中，则不进行滚动

                // 2. 使用实时维护的sizeMap查找并计算目标元素的offset （非准确值）
                // Use the real-time maintained sizeMap to find and calculate the offset of the target element
                scrollTo(index)
            }
    }

    fun animateTo(key: Any) {
        keyEvent.tryEmit(key)
    }

    private fun doScroll(
        targetOffset: Float,
        isExactScroll: Boolean = false
    ) {
        animator.cancel()
        exactAnimation = isExactScroll
        currentValue.floatValue = 0f
        targetValue.floatValue = targetOffset

        animator.animateToFinalPosition(targetOffset)
    }

    private suspend fun scrollTo(index: Int) = withContext(Dispatchers.Unconfined) {
        if (!isActive) return@withContext
        val firstVisibleIndex = listState.firstVisibleItemIndex
        val firstVisibleOffset = listState.firstVisibleItemScrollOffset
        targetRange.value = minOf(firstVisibleIndex, index)..maxOf(firstVisibleIndex, index)

        // 计算方向乘数，向下滚动则为正数
        // calculate the direction multiplier, if scrolling down, it's positive
        val forwardMultiple = if (index >= firstVisibleIndex) 1f else -1f

        if (!isActive) return@withContext
        // 计算目标距离，若未缓存有相应位置的值，则计算使用平均值
        // calculate the target offset，if these no value cached then use the average value
        val sizeAverage = sizeMap.values.average().toInt()
        val sizeSum = targetRange.value.sumOf {
            if (it == targetRange.value.last) return@sumOf 0
            sizeMap.getOrPut(it) { sizeAverage }
        }
        val spacingSum = (targetRange.value.last - targetRange.value.first) *
                listState.layoutInfo.mainAxisItemSpacing.toFloat()
        var offsetTemp = sizeSum + spacingSum

        // 针对firstVisibleItem的边界情况修正offset值
        // fix the offset value for the boundary case of the firstVisibleItem
        offsetTemp -= firstVisibleOffset * forwardMultiple

        // 使用非准确值进行滚动
        // use the non-accurate value for scrolling
        if (!isActive) return@withContext
        doScroll(offsetTemp * forwardMultiple, false)

        println("[calculate target]: ${targetValue.floatValue} -> range: [${targetRange.value.first} -> ${targetRange.value.last}]")
    }
}

@Composable
fun rememberLazyListAnimateScroller(
    listState: LazyListState,
    enableScrollAnimation: () -> Boolean = { true },
    keysKeeper: () -> Collection<Any> = { emptyList() },
): LazyListAnimateScroller {
    val currentValue = remember { mutableFloatStateOf(0f) }
    val targetValue = remember { mutableFloatStateOf(0f) }
    val deltaValue = remember { mutableFloatStateOf(0f) }
    val targetRange = remember { mutableStateOf(IntRange(0, 0)) }
    val sizeMap = remember { mutableStateMapOf<Int, Int>() }
    val enableAnimation = rememberUpdatedState(enableScrollAnimation())

    val scroller = remember {
        LazyListAnimateScroller(
            listState = listState,
            currentValue = currentValue,
            targetValue = targetValue,
            deltaValue = deltaValue,
            targetRange = targetRange,
            sizeMap = sizeMap,
            keysKeeper = keysKeeper
        )
    }

    LaunchedEffect(Unit) {
        snapshotFlow { deltaValue.floatValue }
            .collectLatest {
                if (!enableAnimation.value) return@collectLatest
                listState.scroll { scrollBy(it) }
            }
    }

    LaunchedEffect(Unit) {
        scroller.startLoop(this)
    }

    return scroller
}