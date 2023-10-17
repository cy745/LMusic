package com.lalilu.lmusic.utils.extension

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.compositionLocalOf

val LocalWindowSize = compositionLocalOf<WindowSizeClass> {
    error("WindowSizeClass hasn't been initialized")
}
