package com.lalilu.lmusic.compose

import androidx.compose.material.Surface
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.lalilu.component.base.LocalWindowSize

object DrawerWrapper {

    @Composable
    fun Content(
        windowClass: WindowSizeClass = LocalWindowSize.current,
        mainContent: @Composable () -> Unit,
        secondContent: @Composable () -> Unit,
    ) {
        val density = LocalDensity.current
        val mainContentWidthPx = remember { density.run { 360.dp.toPx() }.toInt() }

        val policy = remember(windowClass.widthSizeClass) {
            when (windowClass.widthSizeClass) {
                WindowWidthSizeClass.Expanded -> rowPolicy(
                    mainContentWidthPx = mainContentWidthPx
                )

                else -> boxPolicy()
            }
        }

        Layout(
            content = {
                Surface { mainContent() }
                secondContent()
            },
            measurePolicy = policy
        )
    }

    private fun rowPolicy(
        mainContentWidthPx: Int,
    ): MeasurePolicy = MeasurePolicy { measurables, constraints ->
        val mainPlaceable = measurables[0]
            .measure(constraints.copy(maxWidth = mainContentWidthPx))

        val secondWidth = constraints.maxWidth - mainContentWidthPx
        val secondPlaceable = measurables[1]
            .measure(constraints.copy(maxWidth = secondWidth))

        layout(constraints.maxWidth, constraints.maxHeight) {
            mainPlaceable.place(0, 0)

            secondPlaceable.place(mainContentWidthPx, 0)
        }
    }

    private fun boxPolicy(): MeasurePolicy = MeasurePolicy { measurables, constraints ->
        val placeables = measurables.map { it.measure(constraints) }

        layout(constraints.maxWidth, constraints.maxHeight) {
            placeables.forEach { placeable ->
                placeable.place(0, 0)
            }
        }
    }
}