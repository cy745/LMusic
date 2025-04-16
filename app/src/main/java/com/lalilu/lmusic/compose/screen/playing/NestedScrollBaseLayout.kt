package com.lalilu.lmusic.compose.screen.playing

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
import androidx.compose.ui.unit.Velocity
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.roundToInt


@Composable
fun NestedScrollBaseLayout(
    draggable: CustomAnchoredDraggableState,
    isLyricScrollEnable: MutableState<Boolean>,
    toolbarContent: @Composable () -> Unit = {},
    dynamicHeaderContent: @Composable (Modifier) -> Unit = { },
    playlistContent: @Composable (Modifier) -> Unit = {},
    overlayContent: @Composable (BoxScope.() -> Unit) = {},
) {
    val haptic = LocalHapticFeedback.current

    BackHandler(draggable.state.value == DragAnchor.Max) {
        if (draggable.state.value == DragAnchor.Max) {
            draggable.animateToState(DragAnchor.Middle)
        }
    }

    val lyricViewNestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                // 取消正在进行的动画事件
                draggable.tryCancel()

                if (
                    !isLyricScrollEnable.value
                    && available.y > 0
                    && source == NestedScrollSource.UserInput
                    && draggable.position.floatValue.toInt()
                    == draggable.getPositionByAnchor(DragAnchor.Max)
                ) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    isLyricScrollEnable.value = true
                }

                return if (isLyricScrollEnable.value) {
                    super.onPreScroll(available, source)
                } else {
                    if (source == NestedScrollSource.UserInput) {
                        draggable.dispatchRawDelta(available.y)
                    }
                    available
                }
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                return if (isLyricScrollEnable.value) {
                    super.onPreScroll(available, source)
                } else {
                    draggable.dispatchRawDelta(available.y)
                    available
                }
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                if (!isLyricScrollEnable.value) {
                    draggable.fling(available.y)
                    return available
                }

                return super.onPreFling(available)
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                draggable.fling(0f)

                return super.onPostFling(consumed, available)
            }
        }
    }

    val playlistNestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                draggable.tryCancel()

                if (available.y < 0f) {
                    return available.copy(y = draggable.dispatchRawDelta(available.y))
                }

                return super.onPreScroll(available, source)
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (available.y > 0f) {
                    val consumedY = draggable.dispatchRawDelta(available.y)

                    if ((available.y - consumedY) > 0.005f && source == NestedScrollSource.SideEffect) {
                        throw CancellationException()
                    }
                    return available.copy(y = consumedY)
                }

                return super.onPostScroll(consumed, available, source)
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                draggable.fling(available.y)

                return if (available.y > 0) {
                    // 向下滑动的情况，消耗剩余的所有速度，避免剩余的速度传递给OverScroll
                    available
                } else {
                    // 向上滑动的情况，将剩余速度继续传递给外部的OverScroll
                    super.onPostFling(consumed, available)
                }
            }
        }
    }

    Box {
        Layout(
            content = {
                toolbarContent()
                dynamicHeaderContent(
                    Modifier.nestedScroll(lyricViewNestedScrollConnection)
                )
                playlistContent(
                    Modifier.nestedScroll(playlistNestedScrollConnection)
                )
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