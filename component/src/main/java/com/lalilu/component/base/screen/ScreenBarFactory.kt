package com.lalilu.component.base.screen

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.currentCompositeKeyHash
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

data class ScreenBarComponent(
    val key: String,
    val content: @Composable () -> Unit
)

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
        isVisible: () -> Boolean,
        onDismiss: () -> Unit,
        onBackPressed: (() -> Unit)?,
        content: @Composable () -> Unit
    ) {
        val key = currentCompositeKeyHash

        LaunchedEffect(isVisible()) {
            if (isVisible()) {
                stack.stack += ScreenBarComponent(
                    key = key.toString(),
                    content = {
                        content.invoke()

                        if (onBackPressed != null) {
                            BackHandler {
                                onDismiss()
                                onBackPressed()
                            }
                        }
                    }
                )
            } else {
                stack.stack = stack.stack
                    .filter { it.key != key.toString() }
            }
        }
    }
}