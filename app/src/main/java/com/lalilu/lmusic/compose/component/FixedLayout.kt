package com.lalilu.lmusic.compose.component

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalConfiguration

/**
 * 一个可以固定布局的Layout组件，用于固定某一组件在旋转状态前后不触发宽高相关的变化
 */
@Composable
fun FixedLayout(
    content: @Composable () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isLandscape by remember(configuration.orientation) {
        derivedStateOf { configuration.orientation == Configuration.ORIENTATION_LANDSCAPE }
    }

    Layout(
        content = content,
        measurePolicy = { measurables, constraints ->
            val cConstraint = if (isLandscape) constraints.copy(
                maxHeight = constraints.maxWidth,
                maxWidth = constraints.maxHeight,
            ) else constraints

            val placeable = measurables.map { it.measure(cConstraint) }

            layout(
                width = cConstraint.maxWidth,
                height = cConstraint.maxHeight
            ) {
                placeable.onEach { it.place(0, 0) }
            }
        }
    )
}