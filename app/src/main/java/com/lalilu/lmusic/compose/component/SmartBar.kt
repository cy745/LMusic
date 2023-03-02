package com.lalilu.lmusic.compose.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.lalilu.lmusic.compose.new_screen.NavBar
import com.lalilu.lmusic.utils.extension.measure
import com.lalilu.lmusic.utils.recomposeHighlighter

object SmartBar {
    private val mainBar: MutableState<(@Composable () -> Unit)?> = mutableStateOf(null)
    private val extraBar: MutableState<(@Composable () -> Unit)?> = mutableStateOf(null)

    val smartBarHeightDpState = mutableStateOf(0.dp)

    @Composable
    @OptIn(ExperimentalAnimationApi::class, ExperimentalLayoutApi::class)
    fun BoxScope.SmartBarContent(modifier: Modifier = Modifier) {
        val density = LocalDensity.current

        Surface(
            modifier = modifier
                .recomposeHighlighter()
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            color = MaterialTheme.colors.background.copy(alpha = 0.95f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .measure { _, height ->
                        smartBarHeightDpState.value = density.run { height.toDp() + 20.dp }
                    }
            ) {
                AnimatedContent(targetState = extraBar.value) {
                    it?.invoke()
                }
                AnimatedContent(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .imePadding(),
                    targetState = WindowInsets.isImeVisible to mainBar.value
                ) { pair ->
                    pair.second.takeIf { !pair.first }?.invoke()
                }
            }
        }
    }

    fun setMainBar(
        content: (@Composable () -> Unit)?
    ): SmartBar = apply {
        mainBar.value = content
    }

    fun setExtraBar(
        content: (@Composable () -> Unit)?
    ): SmartBar = apply {
        extraBar.value = content
    }

    private val extraContentStack = ArrayDeque<StackItem>()
    private val mainContentStack = ArrayDeque<StackItem>()

    @Composable
    fun RegisterExtraBarContent(
        showState: State<Boolean>,
        recoverTo: (@Composable () -> Unit)? = null,
        content: @Composable () -> Unit
    ) {
        val stackItem = remember { StackItem(state = showState, content = content) }

        LaunchedEffect(stackItem.state.value) {
            if (stackItem.state.value) {
                if (!extraContentStack.contains(stackItem)) {
                    extraContentStack.addLast(stackItem)
                    setExtraBar(stackItem.content)
                }
            } else {
                if (extraContentStack.lastOrNull() == stackItem) {
                    extraContentStack.removeLast()
                    setExtraBar(extraContentStack.lastOrNull()?.content ?: recoverTo)
                }
            }
        }
        DisposableEffect(Unit) {
            onDispose {
                if (extraContentStack.lastOrNull() == stackItem) {
                    extraContentStack.removeLast()
                    setExtraBar(extraContentStack.lastOrNull()?.content ?: recoverTo)
                } else {
                    extraContentStack.remove(stackItem)
                }
            }
        }
    }

    @Composable
    fun RegisterMainBarContent(
        showState: State<Boolean>,
        recoverTo: @Composable () -> Unit = NavBar.content,
        content: @Composable () -> Unit
    ) {
        val stackItem = remember { StackItem(state = showState, content = content) }

        LaunchedEffect(stackItem.state.value) {
            if (stackItem.state.value) {
                if (!mainContentStack.contains(stackItem)) {
                    mainContentStack.addLast(stackItem)
                    setMainBar(stackItem.content)
                }
            } else {
                if (mainContentStack.lastOrNull() == stackItem) {
                    mainContentStack.removeLast()
                    setMainBar(mainContentStack.lastOrNull()?.content ?: recoverTo)
                }
            }
        }
        DisposableEffect(Unit) {
            onDispose {
                if (mainContentStack.lastOrNull() == stackItem) {
                    mainContentStack.removeLast()
                    setMainBar(mainContentStack.lastOrNull()?.content ?: recoverTo)
                } else {
                    mainContentStack.remove(stackItem)
                }
            }
        }
    }

    private class StackItem(
        val state: State<Boolean>,
        val key: String = state.hashCode().toString(),
        val content: @Composable () -> Unit
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as StackItem

            if (key != other.key) return false

            return true
        }

        override fun hashCode(): Int {
            return key.hashCode()
        }
    }
}