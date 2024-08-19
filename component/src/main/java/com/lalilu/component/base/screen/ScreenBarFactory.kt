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


class ComponentStack {
    var stack: List<ScreenBarComponent> by mutableStateOf(emptyList())

    companion object {
        private val instanceMap = mutableStateMapOf<ScreenBarFactory, ComponentStack>()

        fun getInstance(attach: ScreenBarFactory): ComponentStack {
            return instanceMap.getOrPut(attach) { ComponentStack() }
        }
    }
}

interface ScreenBarFactory {
    private val stack: ComponentStack
        get() = ComponentStack.getInstance(this)

    @Composable
    fun content(): ScreenBarComponent? {
        return stack.stack.lastOrNull()
    }

    @Composable
    fun RegisterContent(
        isVisible: MutableState<Boolean>,
        onBackPressed: (() -> Unit)?,
        content: @Composable () -> Unit
    ) {
        LaunchedEffect(isVisible.value) {
            if (isVisible.value) {
                stack.stack += ScreenBarComponent(
                    state = isVisible,
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