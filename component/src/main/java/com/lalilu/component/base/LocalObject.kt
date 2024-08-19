package com.lalilu.component.base

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.unit.dp

val LocalSmartBarPadding = compositionLocalOf { mutableStateOf(PaddingValues(0.dp)) }

val LocalWindowSize = compositionLocalOf<WindowSizeClass> {
    error("WindowSizeClass hasn't been initialized")
}
