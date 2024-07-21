package com.lalilu.component.base

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import cafe.adriel.voyager.core.screen.Screen
import com.lalilu.component.base.screen.ScreenInfoFactory
import kotlinx.coroutines.CoroutineScope

/**
 * 定义一个页面的信息
 */
data class ScreenInfo(
    @StringRes val title: Int,
    @DrawableRes val icon: Int? = null,
    val immerseStatusBar: Boolean = true,
)

/**
 * 定义某个页面可执行的动作
 */
sealed interface ScreenAction {
    data class StaticAction(
        @StringRes val title: Int,
        @DrawableRes val icon: Int? = null,
        @StringRes val info: Int? = null,
        val color: Color = Color.White,
        val fitImePadding: Boolean = false,
        val isLongClickAction: Boolean = false,
        val onAction: () -> Unit
    ) : ScreenAction

    data class ComposeAction(
        val content: @Composable () -> Unit
    ) : ScreenAction
}

data class ScreenBarComponent(
    val state: MutableState<Boolean>,
    val showMask: Boolean,
    val showBackground: Boolean,
    val key: String = state.hashCode().toString(),
    val content: @Composable () -> Unit
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ScreenBarComponent

        return key == other.key
    }

    override fun hashCode(): Int {
        return key.hashCode()
    }
}

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

