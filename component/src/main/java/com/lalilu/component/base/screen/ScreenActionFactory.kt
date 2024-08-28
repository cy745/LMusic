package com.lalilu.component.base.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

@Stable
@Immutable
data class ActionContext(
    val isFullyExpanded: Boolean = false
)

sealed class ScreenAction {
    @Stable
    data class Static(
        val title: @Composable () -> String,
        val subTitle: @Composable () -> String? = { null },
        val color: @Composable () -> Color = { Color.White },
        val icon: @Composable () -> ImageVector? = { null },
        val dotColor: @Composable () -> Color? = { null },
        val onAction: () -> Unit = {}
    ) : ScreenAction()

    @Stable
    data class Dynamic(
        val content: @Composable (ActionContext) -> Unit
    ) : ScreenAction()
}

interface ScreenActionFactory {

    @Composable
    fun provideScreenActions(): List<ScreenAction> {
        return emptyList()
    }
}