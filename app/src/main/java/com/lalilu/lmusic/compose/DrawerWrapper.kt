package com.lalilu.lmusic.compose

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.DraggableState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp

object DrawerWrapper {

    val reverseLayout = mutableStateOf(false)
    val offsetX = mutableStateOf(0f)

    @Composable
    fun DefaultSpacerContent() {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(20.dp)
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = DraggableState { deltaX ->
                        // TODO 若已经到达边界了，则不应再记录会超出范围的值
                        offsetX.value += deltaX * if (reverseLayout.value) -1f else 1f
                    }
                )
        ) {
            Spacer(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxHeight(0.2f)
                    .width(4.dp)
                    .background(
                        color = Color.DarkGray,
                        shape = RoundedCornerShape(4.dp)
                    )
            )
        }
    }

    @Composable
    fun Content(
        isPad: () -> Boolean = { false },
        isLandscape: () -> Boolean = { false },
        mainContent: @Composable () -> Unit,
        spacerContent: @Composable () -> Unit = { DefaultSpacerContent() },
        secondContent: @Composable () -> Unit,
    ) {
        val minWidthForMainContent = LocalDensity.current.run { 360.dp.toPx() }
        val maxWidthForMainContent = LocalDensity.current.run { 480.dp.toPx() }

        val animateProgress = animateFloatAsState(
            label = "reverseLayout",
            targetValue = if (reverseLayout.value) 1f else 0f,
            animationSpec = spring(
                dampingRatio = 0.9f,
                stiffness = Spring.StiffnessLow
            )
        )

        val policy = remember(isPad(), isLandscape()) {
            when {
                isPad() && isLandscape() -> drawerMeasurePolicy(
                    minWidthForMainContent = minWidthForMainContent,
                    animateProgress = animateProgress.value
                )

                isPad() -> boxMeasurePolicy(targetIndex = listOf(0, 2))

                // 普通手机端则Fixed，避免宽高变化影响界面
                else -> fixedMeasurePolicy(
                    isLandscape = isLandscape(),
                    targetIndex = listOf(0, 2)
                )
            }
        }

        Layout(
            content = {
                mainContent()
                spacerContent()
                secondContent()
            },
            measurePolicy = policy
        )
    }

    private fun boxMeasurePolicy(
        targetIndex: List<Int> = listOf(0)
    ) = MeasurePolicy { measurables, constraints ->
        val placeable = targetIndex.mapNotNull { measurables.getOrNull(it) }
            .map { it.measure(constraints) }

        layout(
            width = constraints.maxWidth,
            height = constraints.maxHeight
        ) {
            placeable.onEach { it.place(0, 0) }
        }
    }

    private fun drawerMeasurePolicy(
        minWidthForMainContent: Float,
        animateProgress: Float
    ) = MeasurePolicy { measurables, constraints ->
        // TODO 限制targetWidth的最大和最小值
        val targetWidth = minWidthForMainContent + offsetX.value

        val spacer = measurables.getOrNull(1)?.measure(constraints)
        val main = measurables.getOrNull(0)
            ?.measure(
                constraints.copy(
                    maxWidth = targetWidth.toInt(),
                    minWidth = targetWidth.toInt()
                )
            )

        val lastXSpace = constraints.maxWidth - (spacer?.width ?: 0) - (main?.width ?: 0)
        val second = measurables.getOrNull(2)
            ?.measure(constraints.copy(maxWidth = lastXSpace))

        layout(constraints.maxWidth, constraints.maxHeight) {
            val mainX = lerp(
                start = 0,
                stop = (second?.width ?: 0) + (spacer?.width ?: 0),
                fraction = animateProgress
            )
            val spaceX = lerp(
                start = main?.width ?: 0,
                stop = second?.width ?: 0,
                fraction = animateProgress
            )
            val secondX = lerp(
                start = (main?.width ?: 0) + (spacer?.width ?: 0),
                stop = 0,
                fraction = animateProgress
            )

            main?.place(x = mainX, y = 0, zIndex = 20f)
            spacer?.place(x = spaceX, y = 0, zIndex = 10f)
            second?.place(x = secondX, y = 0, zIndex = 0f)
        }
    }

    private fun fixedMeasurePolicy(
        targetIndex: List<Int> = listOf(0),
        isLandscape: Boolean
    ) = MeasurePolicy { measurables, constraints ->
        val cConstraint = if (isLandscape) constraints.copy(
            maxHeight = constraints.maxWidth,
            maxWidth = constraints.maxHeight,
            minHeight = constraints.minWidth,
            minWidth = constraints.minHeight
        ) else constraints

        val placeable = targetIndex.mapNotNull { measurables.getOrNull(it) }
            .map { it.measure(cConstraint) }

        layout(
            width = cConstraint.maxWidth,
            height = cConstraint.maxHeight
        ) {
            placeable.onEach { it.place(0, 0) }
        }
    }
}