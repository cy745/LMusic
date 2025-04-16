package com.lalilu.component.base.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

data class ScreenInfo(
    val title: @Composable () -> String,
    val icon: ImageVector? = null
)

interface ScreenInfoFactory {

    @Composable
    fun provideScreenInfo(): ScreenInfo
}