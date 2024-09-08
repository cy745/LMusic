package com.lalilu.component.extension

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import androidx.dynamicanimation.animation.springAnimationOf
import androidx.dynamicanimation.animation.withSpringForceProperties
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LazyListAnimateScroller internal constructor(
    private val keysKeeper: () -> Collection<Any>,
    private val enable: () -> Boolean = { true },
    private val listState: LazyListState,
    private val scope: CoroutineScope
) {
    private val currentValue = mutableFloatStateOf(0f)
    private val targetValue = mutableFloatStateOf(0f)
    private val targetRange = mutableStateOf(IntRange(0, 0))
    private val sizeMap = mutableStateMapOf<Int, Int>()

    private var exactAnimation: Boolean = false
    private var targetIndex: Int = -1

    private val animator: SpringAnimation = springAnimationOf(
        getter = { currentValue.floatValue },
        setter = {
            onScroll(it - currentValue.floatValue)
            currentValue.floatValue = it
        },
        finalPosition = 0f
    ).withSpringForceProperties {
        dampingRatio = SpringForce.DAMPING_RATIO_NO_BOUNCY
        stiffness = SpringForce.STIFFNESS_VERY_LOW
    }.addUpdateListener { animation, value, velocity ->
        val percent = if (targetValue.floatValue <= 0) 0f else value / targetValue.floatValue

        if (percent > 0.5f && !exactAnimation && targetIndex != -1) {
            val tempIndex = targetIndex
            scope.launch { scrollTo(tempIndex) }
            targetIndex = -1
        }
    }.addEndListener { animation, canceled, value, velocity ->
        if (!canceled) {
            targetRange.value = IntRange.EMPTY
        }
    }

    private fun onScroll(dy: Float) {
        if (!enable()) return

        scope.launch {
            try {
                listState.scroll { scrollBy(dy) }
            } catch (e: Exception) {
                // 若是CancellationException，则停止animator动画
                if (e is CancellationException) {
                    animator.cancel()
                }
            }
        }
    }

    /**
     * 启动循环任务，用于监听可见元素列表的变化，并计算目标元素的偏移量
     */
    internal suspend fun startLoop(scope: CoroutineScope) = withContext(scope.coroutineContext) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo }
            .distinctUntilChanged()
            .onEach { list -> list.forEach { sizeMap[it.index] = it.size } }
            .launchIn(this)
    }

    fun animateTo(key: Any) = scope.launch {
        // 1. 从当前可见元素直接查找offset （准确值）
        // get the offset directly from the visibleItemsInfo
        val offset = listState.layoutInfo.visibleItemsInfo
            .firstOrNull { it.key == key }
            ?.offset

        if (offset != null) {
            doScroll(offset.toFloat(), true)
            return@launch
        }

        val index = keysKeeper().indexOfFirst { it == key }
        if (index == -1) return@launch // 元素不存在keys列表中，则不进行滚动

        // 2. 使用实时维护的sizeMap查找并计算目标元素的offset （非准确值）
        // Use the real-time maintained sizeMap to find and calculate the offset of the target element
        scrollTo(index)
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
        targetIndex = index
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
    val enableAnimation = rememberUpdatedState(enableScrollAnimation())
    val scope = rememberCoroutineScope()

    val scroller = remember {
        LazyListAnimateScroller(
            listState = listState,
            enable = { enableAnimation.value },
            keysKeeper = keysKeeper,
            scope = scope
        )
    }

    LaunchedEffect(Unit) {
        scroller.startLoop(this)
    }

    return scroller
}