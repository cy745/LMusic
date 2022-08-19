package com.lalilu.lmusic.utils

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.compositionLocalOf
import androidx.navigation.NavHostController

val LocalWindowSize = compositionLocalOf<WindowSizeClass> {
    error("WindowSizeClass hasn't been initialized")
}

val LocalNavigatorHost = compositionLocalOf<NavHostController> {
    error("NavController hasn't not presented")
}