package com.lalilu.lmusic.utils.extension

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.unit.dp

val LocalWindowSize = compositionLocalOf<WindowSizeClass> {
    error("WindowSizeClass hasn't been initialized")
}

val LocalPaddingValue = compositionLocalOf { mutableStateOf(PaddingValues(0.dp)) }