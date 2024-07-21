package com.lalilu.component.base.screen

import androidx.compose.runtime.Composable
import com.lalilu.component.base.ScreenAction


interface ScreenActionFactory {

    @Composable
    fun provideScreenActions(): List<ScreenAction> {
        return emptyList()
    }
}