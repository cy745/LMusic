package com.lalilu.component.extension

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LazyListAnimateScroller internal constructor(
    private val keysKeeper: () -> Collection<Any>,
    private val enable: () -> Boolean = { true },
    private val listState: LazyListState,
    private val scope: CoroutineScope
) {
    /**
     * 滚动任务，用于缓存每一次的主动滚动
     *
     * @param key               目标元素key
     * @param offsetBlock       当目标元素可见时，计算与顶部偏移量的回调
     * @param isStickyHeader    判断元素是否StickyHeader
     * @param onEnd             滚动结束的回调
     */
    data class ScrollTask(
        val key: Any,
        val onEnd: (isCanceled: Boolean) -> Unit = {},
        val isStickyHeader: (LazyListItemInfo) -> Boolean = { false },
        val offsetBlock: (LazyListItemInfo) -> Int = { 0 }
    ) {
        var isRectified = false
        var isFinished = false
        var targetIndex = -1
    }

    private val animation by lazy { Animatable(0f, Float.VectorConverter) }
    private var task: ScrollTask? = null
    private var targetRange: IntRange = IntRange(0, 0)
    private val sizeMap = mutableMapOf<Int, Int>()

    /**
     * 启动循环任务，用于监听可见元素列表的变化，并计算目标元素的偏移量
     */
    internal suspend fun startLoop(scope: CoroutineScope) = withContext(scope.coroutineContext) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo }
            .distinctUntilChanged()
            .onEach { list -> list.forEach { sizeMap[it.index] = it.size } }
            .launchIn(this)
    }

    fun animateTo(
        key: Any,
        onEnd: (Boolean) -> Unit = {},
        isStickyHeader: (LazyListItemInfo) -> Boolean = { false },
        offsetBlock: (LazyListItemInfo) -> Int = { 0 }
    ) = animateTo(
        ScrollTask(
            key = key,
            onEnd = onEnd,
            isStickyHeader = isStickyHeader,
            offsetBlock = offsetBlock
        )
    )

    fun animateTo(scrollTask: ScrollTask) {
        task = scrollTask
        calcAndStartAnimation(scrollTask)
    }

    private fun calcAndStartAnimation(task: ScrollTask) {
        // 1. 从当前可见元素直接查找offset （准确值）
        // get the offset directly from the visibleItemsInfo
        val targetItem = listState.layoutInfo.visibleItemsInfo
            .firstOrNull { it.key == task.key }

        val targetOffset = targetItem?.let { item ->
            // 若非StickyHeader，则其offset即为准确的滚动位移值
            if (!task.isStickyHeader(item)) {
                return@let item.offset
            }

            // 若为StickyHeader则使用其下一个元素的offset - 当前元素的size计算获取
            listState.layoutInfo.visibleItemsInfo
                .firstOrNull { it.index == (item.index + 1) }
                ?.let { it.offset - item.size - listState.layoutInfo.mainAxisItemSpacing }
        }

        // 若获取到offset，则直接进行滚动
        targetOffset?.let { offset ->
            doScroll(offset.toFloat() + task.offsetBlock(targetItem))
            return
        }

        // 若未获取到offset，则使用keys列表查找目标元素，并计算其offset
        val index = keysKeeper().indexOfFirst { it == task.key }
        task.targetIndex = index

        if (index == -1) {
            this.task = null
            return  // 元素不存在keys列表中，则不进行滚动
        }

        // 2. 使用实时维护的sizeMap查找并计算目标元素的offset （非准确值）
        // Use the real-time maintained sizeMap to find and calculate the offset of the target element
        scrollTo(index)
        return
    }

    private fun doScroll(targetOffset: Float) {
        scope.launch {
            // 获取上一次滚动时最终的滚动速度
            val oldVelocity = animation.velocity
            animation.snapTo(0f)

            var lastValue = 0f
            animation.animateTo(
                targetValue = targetOffset,
                animationSpec = spring(stiffness = Spring.StiffnessVeryLow),
                initialVelocity = oldVelocity
            ) {
                val dy = value - lastValue
                lastValue = value

                scope.launch {
                    try {
                        listState.scroll { scrollBy(dy) }
                    } catch (e: Exception) {
                        if (e is CancellationException) {
                            animation.stop()
                        }
                    }
                }

                // TODO 中途纠正
            }

            // TODO 末端纠正
        }
    }

    private fun scrollTo(index: Int) {
        val firstVisibleIndex = listState.firstVisibleItemIndex
        val firstVisibleOffset = listState.firstVisibleItemScrollOffset
        targetRange = minOf(firstVisibleIndex, index)..maxOf(firstVisibleIndex, index)

        // 计算方向乘数，向下滚动则为正数
        // calculate the direction multiplier, if scrolling down, it's positive
        val forwardMultiple = if (index >= firstVisibleIndex) 1f else -1f

        // 计算目标距离，若未缓存有相应位置的值，则计算使用平均值
        // calculate the target offset，if these no value cached then use the average value
        val sizeAverage = sizeMap.values.average().toInt()
        val sizeSum = targetRange.sumOf {
            if (it == targetRange.last) return@sumOf 0
            sizeMap.getOrPut(it) { sizeAverage }
        }
        val spacingSum = (targetRange.last - targetRange.first) *
                listState.layoutInfo.mainAxisItemSpacing.toFloat()
        var offsetTemp = sizeSum + spacingSum

        // 针对firstVisibleItem的边界情况修正offset值
        // fix the offset value for the boundary case of the firstVisibleItem
        offsetTemp -= firstVisibleOffset * forwardMultiple

        // 使用非准确值进行滚动
        // use the non-accurate value for scrolling
        doScroll(offsetTemp * forwardMultiple)
    }

    private fun isItemVisible(task: ScrollTask, index: Int): Boolean {
        val startIndex = listState.firstVisibleItemIndex
        val endIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0

        val isVisible = index in startIndex..endIndex
        if (!isVisible) return false

        val targetItem = listState.layoutInfo.visibleItemsInfo
            .firstOrNull { it.index == index }
            ?: return false

        val isStickyHeader = task.isStickyHeader(targetItem)
        if (!isStickyHeader) return true

        return (index + 1) in startIndex..endIndex
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