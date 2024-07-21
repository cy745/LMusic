package com.lalilu.component.base.screen

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable

data class ScreenInfo(
    @StringRes val title: Int,
    @DrawableRes val icon: Int? = null,
)

interface ScreenInfoFactory {

    @Composable
    fun provideScreenInfo(): ScreenInfo
}