package com.lalilu.component.base.screen

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.lalilu.component.base.ScreenBarComponent


private class ExtraComponentStack {
    var stack: List<ScreenBarComponent> by mutableStateOf(emptyList())

    companion object {
        private val instanceMap = mutableStateMapOf<ScreenExtraBarFactory, ExtraComponentStack>()

        fun getInstance(attach: ScreenExtraBarFactory): ExtraComponentStack {
            return instanceMap.getOrPut(attach) { ExtraComponentStack() }
        }
    }
}

@Deprecated("移除")
interface ScreenExtraBarFactory {
    private val stack: ExtraComponentStack
        get() = ExtraComponentStack.getInstance(this)

    @Composable
    fun content(): ScreenBarComponent? {
        return stack.stack.lastOrNull()
    }

    @Composable
    fun RegisterExtraContent(
        isVisible: MutableState<Boolean>,
        onBackPressed: (() -> Unit)?,
        content: @Composable () -> Unit
    ) {
        LaunchedEffect(isVisible.value) {
            if (isVisible.value) {
                stack.stack += ScreenBarComponent(
                    state = isVisible,
                    showMask = false,
                    showBackground = false,
                    content = {
                        content.invoke()

                        if (onBackPressed != null) {
                            BackHandler {
                                isVisible.value = false
                                onBackPressed()
                            }
                        }
                    }
                )
            } else {
                val key = isVisible.hashCode().toString()
                stack.stack = stack.stack.filter { it.key != key }
            }
        }
    }
}