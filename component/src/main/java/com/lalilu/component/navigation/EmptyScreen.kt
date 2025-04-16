package com.lalilu.component.navigation

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import com.lalilu.component.base.screen.ScreenType

data object EmptyScreen : Screen, ScreenType.Empty {
    private fun readResolve(): Any = EmptyScreen

    @Composable
    override fun Content() {
        Spacer(modifier = Modifier.fillMaxSize())
    }
}