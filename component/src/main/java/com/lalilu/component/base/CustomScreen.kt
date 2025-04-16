package com.lalilu.component.base

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import com.lalilu.component.base.screen.ScreenInfoFactory
import kotlinx.coroutines.CoroutineScope

@Deprecated("弃用，待移除")
interface TabScreen : Screen, ScreenInfoFactory

interface UiState
interface UiAction
interface UiPresenter<T : UiState> : CoroutineScope {
    @Composable
    fun presentState(): T
    fun onAction(action: UiAction)
}
