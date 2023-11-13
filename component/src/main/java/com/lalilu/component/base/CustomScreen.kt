package com.lalilu.component.base

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import cafe.adriel.voyager.core.screen.Screen
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
        val color: Color = Color.Transparent,
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

interface TabScreen : CustomScreen {
    override fun getScreenInfo(): ScreenInfo
}

interface DialogScreen : CustomScreen


interface UiState
interface UiAction
interface UiPresenter<T : UiState> : CoroutineScope {
    @Composable
    fun presentState(): T
    fun onAction(action: UiAction)
}

interface CustomScreen : Screen {
    fun getScreenInfo(): ScreenInfo? = null
}

abstract class DynamicScreen : CustomScreen {
    @delegate:Transient     // Voyager的Screen会被序列化，需要避免自定义的参数参与进序列化
    var actions: List<ScreenAction> by mutableStateOf(emptyList())
        private set

    @delegate:Transient
    var extraContentStack: List<ScreenBarComponent> by mutableStateOf(emptyList())
        private set

    @delegate:Transient
    var mainContentStack: List<ScreenBarComponent> by mutableStateOf(emptyList())
        private set

    @Composable
    fun RegisterActions(actionList: () -> List<ScreenAction>) {
        LaunchedEffect(Unit) {
            actions = actionList()
        }
    }

    @Composable
    fun RegisterExtraContent(
        isVisible: MutableState<Boolean> = remember { mutableStateOf(true) },
        showMask: () -> Boolean = { false },
        showBackground: () -> Boolean = { true },
        onBackPressed: (() -> Unit)? = null,
        content: @Composable () -> Unit
    ) {
        LaunchedEffect(isVisible.value) {
            if (isVisible.value) {
                extraContentStack = extraContentStack.plus(ScreenBarComponent(
                    state = isVisible,
                    showMask = showMask(),
                    showBackground = showBackground(),
                    content = {
                        content.invoke()

                        if (onBackPressed != null) {
                            BackHandler {
                                isVisible.value = false
                                onBackPressed()
                            }
                        }
                    }
                ))
            } else {
                val key = isVisible.hashCode().toString()
                extraContentStack = extraContentStack.filter { it.key != key }
            }
        }
    }

    @Composable
    fun RegisterMainContent(
        isVisible: MutableState<Boolean> = remember { mutableStateOf(true) },
        showMask: () -> Boolean = { false },
        showBackground: () -> Boolean = { true },
        onBackPressed: () -> Unit = {},
        content: @Composable () -> Unit
    ) {
        LaunchedEffect(isVisible.value) {
            if (isVisible.value) {
                mainContentStack = mainContentStack.plus(ScreenBarComponent(
                    state = isVisible,
                    showMask = showMask(),
                    showBackground = showBackground(),
                    content = {
                        content.invoke()
                        BackHandler {
                            isVisible.value = false
                            onBackPressed()
                        }
                    }
                ))
            } else {
                val key = isVisible.hashCode().toString()
                mainContentStack = mainContentStack.filter { it.key != key }
            }
        }
    }
}

