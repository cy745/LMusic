package com.lalilu.component.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.stack.Stack
import cafe.adriel.voyager.navigator.Navigator

interface SheetNavigator : Stack<Screen> {
    val isVisible: Boolean
    fun hide()
    fun show(screen: Screen? = null)
    fun back(enable: Boolean = true)
    fun getNavigator(): Navigator
}

val LocalSheetNavigator: ProvidableCompositionLocal<SheetNavigator> =
    staticCompositionLocalOf { error("SheetNavigator not initialized") }

@Composable
fun BackHandler(
    navigator: SheetNavigator = LocalSheetNavigator.current,
    onBack: () -> Unit
) {
    BackHandler(enabled = navigator.isVisible, onBack = onBack)
}