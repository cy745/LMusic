package com.lalilu.lmusic.compose.screen.playing

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Velocity
import kotlin.math.roundToInt

enum class ScrollingItemType {
    LyricView,
    RecyclerView
}

@Composable
fun NestedScrollBaseLayout(
    draggable: CustomAnchoredDraggableState,
    isLyricScrollEnable: MutableState<Boolean>,
    scrollingItemType: () -> ScrollingItemType? = { null },
    toolbarContent: @Composable () -> Unit = {},
    dynamicHeaderContent: @Composable (Constraints) -> Unit = { },
    recyclerViewContent: @Composable () -> Unit = {},
    overlayContent: @Composable BoxScope.() -> Unit = {},
) {
    val haptic = LocalHapticFeedback.current


    BackHandler(draggable.state.value == DragAnchor.Max) {
        if (draggable.state.value == DragAnchor.Max) {
            draggable.animateToState(DragAnchor.Middle)
        }
    }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override suspend fun onPreFling(available: Velocity): Velocity {
                // 若非RecyclerView的滚动，则消费y轴上的所有速度，避免嵌套滚动事件继续
                if (ScrollingItemType.RecyclerView != scrollingItemType() && !isLyricScrollEnable.value) {
                    draggable.fling(available.y)
                    return available
                }

                return super.onPreFling(available)
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                if (consumed.y != 0f && available.y == 0f) {
                    draggable.fling(0f)
                }
                return super.onPostFling(consumed, available)
            }

            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                // 取消正在进行的动画事件
                draggable.tryCancel()

                return when (scrollingItemType()) {
                    /**
                     * 若是LyricView的滑动事件，则需判断当前LyricView是否处于可拖动歌词的状态
                     *
                     */
                    ScrollingItemType.LyricView -> {
                        if (
                            !isLyricScrollEnable.value
                            && available.y > 0
                            && source == NestedScrollSource.Drag
                            && draggable.position.floatValue.toInt()
                            == draggable.getPositionByAnchor(DragAnchor.Max)
                        ) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            isLyricScrollEnable.value = true
                        }

                        if (isLyricScrollEnable.value) {
                            super.onPreScroll(available, source)
                        } else {
                            if (source == NestedScrollSource.Drag) {
                                draggable.scrollBy(available.y)
                            }
                            available
                        }
                    }

                    /**
                     * 若是RecyclerView的滑动事件，则区分上滑和下滑的情况
                     * 上滑：首先交由draggable消费，剩余的传递给后续的RecyclerView消费
                     * 下滑：直接全部传递给后续的RecyclerView消费
                     */
                    ScrollingItemType.RecyclerView -> {
                        if (available.y < 0) available.copy(y = draggable.scrollBy(available.y))
                        else super.onPreScroll(available, source)
                    }

                    // 前面的条件都不满足，则将该事件全部消费，避免未知的子组件产生动作
                    else -> available
                }
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset = when (scrollingItemType()) {
                ScrollingItemType.LyricView -> {
                    if (isLyricScrollEnable.value) {
                        super.onPreScroll(available, source)
                    } else {
                        draggable.scrollBy(available.y)
                        available
                    }
                }

                ScrollingItemType.RecyclerView -> {
                    if (available.y > 0) available.copy(y = draggable.scrollBy(available.y))
                    else super.onPostScroll(consumed, available, source)
                }

                else -> super.onPostScroll(consumed, available, source)
            }
        }
    }

    BoxWithConstraints {
        Layout(
            modifier = Modifier
                .nestedScroll(nestedScrollConnection),
            content = {
                toolbarContent()
                dynamicHeaderContent(constraints)
                recyclerViewContent()
            }
        ) { measurables, constraints ->
            val toolbar = measurables[0].measure(constraints)
            val background = measurables[1].measure(constraints)

            val cConstraints = constraints
                .copy(maxHeight = constraints.maxHeight - toolbar.height)
            val recyclerView = measurables[2].measure(cConstraints)

            draggable.updateAnchor(
                min = toolbar.height,
                middle = constraints.maxWidth
                    .coerceAtMost(constraints.maxHeight / 2), // 限制中间的锚点不能超过容器高度的一半
                max = constraints.maxHeight
            )

            layout(
                width = constraints.maxWidth,
                height = constraints.maxHeight
            ) {
                val animateOffset = draggable.position.floatValue.roundToInt()
                    .coerceIn(toolbar.height, constraints.maxHeight)

                background.place(0, animateOffset - background.height)
                toolbar.place(0, animateOffset - toolbar.height)
                recyclerView.place(0, animateOffset)
            }
        }

        overlayContent()
    }
}