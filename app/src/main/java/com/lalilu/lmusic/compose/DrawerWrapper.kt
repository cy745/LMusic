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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
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

        Layout(
            content = {
                mainContent()
                spacerContent()
                secondContent()
            },
            measurePolicy = { measurables, constraints ->
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
                        fraction = animateProgress.value
                    )
                    val spaceX = lerp(
                        start = main?.width ?: 0,
                        stop = second?.width ?: 0,
                        fraction = animateProgress.value
                    )
                    val secondX = lerp(
                        start = (main?.width ?: 0) + (spacer?.width ?: 0),
                        stop = 0,
                        fraction = animateProgress.value
                    )

                    main?.place(x = mainX, y = 0, zIndex = 20f)
                    spacer?.place(x = spaceX, y = 0, zIndex = 10f)
                    second?.place(x = secondX, y = 0, zIndex = 0f)
                }
            }
        )
    }
}