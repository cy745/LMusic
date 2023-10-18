package com.lalilu.lmusic.compose

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen

interface CustomScreen : Screen {
    fun getExtraContent(): (@Composable () -> Unit)? = null
    fun getScreenInfo(): ScreenInfo? = null
}

interface TabScreen : CustomScreen {
    override fun getScreenInfo(): ScreenInfo
}

interface DialogScreen : CustomScreen

data class ScreenInfo(
    @StringRes val title: Int,
    @DrawableRes val icon: Int? = null,
    val immerseStatusBar: Boolean = true,
)