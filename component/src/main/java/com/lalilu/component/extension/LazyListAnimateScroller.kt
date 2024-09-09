package com.lalilu.component.extension

import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import androidx.dynamicanimation.animation.springAnimationOf
import androidx.dynamicanimation.animation.withSpringForceProperties
import com.blankj.utilcode.util.LogUtils
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
        val offsetBlock: (LazyListState) -> Int = { 0 }
    ) {
        var isRectified = false
        var isFinished = false
        var targetIndex = -1
    }

    private var task: ScrollTask? = null
    private var currentValue: Float = 0f
    private var targetValue: Float = 0f
    private var targetRange: IntRange = IntRange(0, 0)
    private val sizeMap = mutableMapOf<Int, Int>()

    private val animator: SpringAnimation = springAnimationOf(
        getter = { currentValue },
        setter = { onScroll(dy = it - currentValue); currentValue = it },
        finalPosition = 0f
    ).withSpringForceProperties {
        dampingRatio = SpringForce.DAMPING_RATIO_NO_BOUNCY
        stiffness = SpringForce.STIFFNESS_VERY_LOW
    }.addUpdateListener { _, _, _ ->
        task?.apply {
            // 若尚未纠正位移，且目标元素处于可见范围内，则重新启动计算动画位移
            if (!isRectified && isItemVisible(targetIndex)) {
                calcAndStartAnimation()
                isRectified = true
            }
        }
    }.addEndListener { _, canceled, _, _ ->
        task?.apply {
            // 若结束后，目标元素不在可见范围内，则重启计算动画
            if (!canceled && !isRectified && !isItemVisible(targetIndex)) {
                calcAndStartAnimation()
                return@apply
            }

            isFinished = true
            onEnd(canceled)
            task = null
        }
    }

    private fun isItemVisible(index: Int): Boolean {
        val startIndex = listState.firstVisibleItemIndex
        val endIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0

        return index in startIndex..endIndex
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

    fun animateTo(
        key: Any,
        onEnd: (Boolean) -> Unit = {},
        isStickyHeader: (LazyListItemInfo) -> Boolean = { false },
        offsetBlock: (LazyListState) -> Int = { 0 },
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
        calcAndStartAnimation()
    }

    private fun calcAndStartAnimation() = scope.launch {
        // 若当前没有滚动任务，则不继续执行
        task ?: return@launch

        val key = task!!.key
        val offsetBlock = task!!.offsetBlock

        // 1. 从当前可见元素直接查找offset （准确值）
        // get the offset directly from the visibleItemsInfo
        val tempIndex = listState.layoutInfo.visibleItemsInfo.indexOfFirst { it.key == key }
        val targetItem = listState.layoutInfo.visibleItemsInfo.getOrNull(tempIndex)

        val targetOffset = targetItem?.let { item ->
            val isSticky = task!!.isStickyHeader.invoke(item)

            // 若目标元素不是stickyHeader，则直接返回offset
            if (!isSticky) {
                return@let item.offset
            }

            // 若目标时是stickyHeader，则查找下一个元素，
            // 若下一个元素的offset等于当前元素的offset + size，
            // 则返回当前元素的offset
            val nextItem = listState.layoutInfo.visibleItemsInfo.getOrNull(tempIndex + 1)
            // TODO 待完善StickyHeader Item的offset计算逻辑
            LogUtils.i("nexItem: ${nextItem?.offset} == ${item.offset} + ${item.size}")
            if (nextItem != null && nextItem.offset == (item.offset + item.size)) {
                return@let item.offset
            }
            null
        }

        if (targetOffset != null) {
            doScroll(targetOffset.toFloat() + offsetBlock(listState))
            return@launch
        }

        val index = keysKeeper().indexOfFirst { it == key }
        if (index == -1) {
            task = null
            return@launch // 元素不存在keys列表中，则不进行滚动
        }

        // 2. 使用实时维护的sizeMap查找并计算目标元素的offset （非准确值）
        // Use the real-time maintained sizeMap to find and calculate the offset of the target element
        task?.targetIndex = index
        scrollTo(index)
    }

    private fun doScroll(
        targetOffset: Float,
    ) {
        animator.cancel()

        currentValue = 0f
        targetValue = targetOffset

        animator.animateToFinalPosition(targetOffset)
    }

    private suspend fun scrollTo(index: Int) = withContext(Dispatchers.Unconfined) {
        if (!isActive) return@withContext
        val firstVisibleIndex = listState.firstVisibleItemIndex
        val firstVisibleOffset = listState.firstVisibleItemScrollOffset
        targetRange = minOf(firstVisibleIndex, index)..maxOf(firstVisibleIndex, index)

        // 计算方向乘数，向下滚动则为正数
        // calculate the direction multiplier, if scrolling down, it's positive
        val forwardMultiple = if (index >= firstVisibleIndex) 1f else -1f

        if (!isActive) return@withContext
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
        if (!isActive) return@withContext
        doScroll(offsetTemp * forwardMultiple)
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