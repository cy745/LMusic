package com.lalilu.component.base

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import cafe.adriel.voyager.core.screen.Screen
import com.lalilu.component.base.screen.ScreenInfoFactory
import kotlinx.coroutines.CoroutineScope

/**
 * 定义一个页面的信息
 */
@Deprecated("弃用", replaceWith = ReplaceWith("com.lalilu.component.base.screen.ScreenInfo"))
data class ScreenInfo(
    @StringRes val title: Int,
    @DrawableRes val icon: Int? = null,
    val immerseStatusBar: Boolean = true,
)

/**
 * 定义某个页面可执行的动作
 */
@Deprecated("弃用")
sealed interface ScreenAction {
    data class StaticAction(
        @StringRes val title: Int,
        @DrawableRes val icon: Int? = null,
        @StringRes val info: Int? = null,
        val color: Color = Color.White,
        val isLongClickAction: Boolean = false,
        val onAction: () -> Unit
    ) : ScreenAction

    data class ComposeAction(
        val content: @Composable (Modifier) -> Unit
    ) : ScreenAction
}

data class ScreenBarComponent(
    val key: String,
    val content: @Composable () -> Unit
)

interface CustomScreen : Screen {
    fun getScreenInfo(): ScreenInfo? = null
}

interface TabScreen : Screen, ScreenInfoFactory
interface DialogScreen : CustomScreen


interface UiState
interface UiAction
interface UiPresenter<T : UiState> : CoroutineScope {
    @Composable
    fun presentState(): T
    fun onAction(action: UiAction)
}

@Deprecated("TODO 替换完成后删除")
abstract class DynamicScreen : CustomScreen {
    @Composable
    open fun registerActions(): List<ScreenAction> {
        return remember { emptyList() }
    }
}

