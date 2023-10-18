package com.lalilu.lmusic.compose.component

import android.view.MotionEvent.ACTION_CANCEL
import android.view.MotionEvent.ACTION_UP
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.lalilu.lmusic.utils.extension.measureHeight

object SmartBar {
    private val mainBar: MutableState<(@Composable () -> Unit)?> = mutableStateOf(null)
    private val extraBar: MutableState<(@Composable () -> Unit)?> = mutableStateOf(null)
    private val showMask: MutableState<Boolean> = mutableStateOf(false)
    private val showBackground: MutableState<Boolean> = mutableStateOf(true)

    val smartBarHeightDpState = mutableStateOf(0.dp)

    @Composable
    @OptIn(
        ExperimentalLayoutApi::class,
        ExperimentalComposeUiApi::class
    )
    fun BoxScope.SmartBarContent(modifier: Modifier = Modifier) {
//        LaunchedEffect(Unit) {
//            setMainBar(content = NavBar.content)
//        }

        val density = LocalDensity.current
        val backPressDispatcher = LocalOnBackPressedDispatcherOwner.current
        val maskColorUp = animateColorAsState(
            if (showMask.value) Color.Black.copy(alpha = 0.4f) else Color.Transparent
        )
        val maskColorBottom = animateColorAsState(
            if (showMask.value) Color.Black.copy(alpha = 0.7f) else Color.Transparent
        )
        val backgroundColor = animateColorAsState(
            if (showBackground.value) MaterialTheme.colors.background.copy(alpha = 0.95f) else Color.Transparent
        )

        // Mask遮罩层，点击后即消失
        Spacer(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            maskColorUp.value,
                            maskColorBottom.value
                        )
                    )
                )
                .fillMaxSize()
                // 监听触摸时间，若为ACTION_UP或ACTION_CANCEL则触发返回事件
                // 返回事件会将该StackItem移除，同时触发SmartBar的状态变化
                .pointerInteropFilter {
                    if (showMask.value && (it.action == ACTION_UP || it.action == ACTION_CANCEL)) {
                        backPressDispatcher?.onBackPressedDispatcher?.onBackPressed()
                    }
                    showMask.value
                }
        )

        Column(
            modifier = modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(color = backgroundColor.value)
                .measureHeight { _, height ->
                    smartBarHeightDpState.value = density.run { height.toDp() }
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

    fun setMainBar(
        showMask: Boolean = false,
        showBackground: Boolean = true,
        content: (@Composable () -> Unit)?,
    ): SmartBar = apply {
        this.showMask.value = showMask
        this.showBackground.value = showBackground
        mainBar.value = content
    }

    fun setExtraBar(
        showMask: Boolean = false,
        showBackground: Boolean = true,
        content: (@Composable () -> Unit)?,
    ): SmartBar = apply {
        this.showMask.value = showMask
        this.showBackground.value = showBackground
        extraBar.value = content
    }

    private fun setMainBar(
        stackItem: StackItem?,
        recoverTo: @Composable (() -> Unit)? = null,
    ) = setMainBar(
        showMask = stackItem?.showMask ?: false,
        showBackground = stackItem?.showBackground ?: true,
        content = stackItem?.content ?: recoverTo
    )

    private fun setExtraBar(
        stackItem: StackItem?,
        recoverTo: @Composable (() -> Unit)? = null,
    ) = setExtraBar(
        showMask = stackItem?.showMask ?: false,
        showBackground = stackItem?.showBackground ?: true,
        content = stackItem?.content ?: recoverTo
    )

    private val extraContentStack = ArrayDeque<StackItem>()
    private val mainContentStack = ArrayDeque<StackItem>()

    @Composable
    fun RegisterExtraBarContent(
        showState: State<Boolean>,
        showMask: Boolean = false,
        showBackground: Boolean = true,
        recoverTo: (@Composable () -> Unit)? = null,
        content: @Composable () -> Unit,
    ) {
        val stackItem = remember {
            StackItem(
                state = showState,
                showMask = showMask,
                showBackground = showBackground,
                content = content
            )
        }
        LaunchedEffect(stackItem.state.value) {
            if (stackItem.state.value) {
                if (!extraContentStack.contains(stackItem)) {
                    extraContentStack.addLast(stackItem)
                    setExtraBar(stackItem, recoverTo)
                }
            } else {
                if (extraContentStack.lastOrNull() == stackItem) {
                    extraContentStack.removeLast()
                    setExtraBar(extraContentStack.lastOrNull(), recoverTo)
                }
            }
        }
        DisposableEffect(Unit) {
            onDispose {
                if (extraContentStack.lastOrNull() == stackItem) {
                    extraContentStack.removeLast()
                    setExtraBar(extraContentStack.lastOrNull(), recoverTo)
                } else {
                    extraContentStack.remove(stackItem)
                }
            }
        }
    }

    @Composable
    fun RegisterMainBarContent(
        showState: State<Boolean>,
        showMask: Boolean = false,
        showBackground: Boolean = true,
        recoverTo: @Composable () -> Unit = { },
        content: @Composable () -> Unit,
    ) {
        val stackItem = remember {
            StackItem(
                state = showState,
                showMask = showMask,
                showBackground = showBackground,
                content = content
            )
        }

        LaunchedEffect(stackItem.state.value) {
            if (stackItem.state.value) {
                if (!mainContentStack.contains(stackItem)) {
                    mainContentStack.addLast(stackItem)
                    setMainBar(stackItem, recoverTo)
                }
            } else {
                if (mainContentStack.lastOrNull() == stackItem) {
                    mainContentStack.removeLast()
                    setMainBar(mainContentStack.lastOrNull(), recoverTo)
                }
            }
        }
        DisposableEffect(Unit) {
            onDispose {
                if (mainContentStack.lastOrNull() == stackItem) {
                    mainContentStack.removeLast()
                    setMainBar(mainContentStack.lastOrNull(), recoverTo)
                } else {
                    mainContentStack.remove(stackItem)
                }
            }
        }
    }

    private class StackItem(
        val state: State<Boolean>,
        val showMask: Boolean = false,
        val showBackground: Boolean = true,
        val key: String = state.hashCode().toString(),
        val content: @Composable () -> Unit,
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