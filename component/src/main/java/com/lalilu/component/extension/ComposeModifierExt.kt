package com.lalilu.component.extension

import android.annotation.SuppressLint
import androidx.compose.foundation.Indication
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch


/**
 * 可自定义长按回调触发时长的Modifier
 */
fun Modifier.longClickable(
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onTap: () -> Unit = {},
    onRelease: () -> Unit = {},
    enableHaptic: Boolean = true,
    longClickMinTimeMillis: Long = 1000L,
    indication: Indication? = null,
    interactionSource: MutableInteractionSource,
): Modifier = composed {
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    this
        .semantics { role = Role.Button }
        .indication(interactionSource, indication ?: LocalIndication.current)
        .hoverable(interactionSource, true)
        .pointerInput(Unit) {
            var timer: Job?

            detectTapGestures(
                onPress = {
                    val press = PressInteraction.Press(it)
                    interactionSource.emit(press)
                    onTap()

                    // tap的瞬间开始计时器
                    timer = scope.launch(Dispatchers.IO) {
                        delay(longClickMinTimeMillis)
                        if (!isActive) return@launch

                        onLongClick()
                        if (enableHaptic) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                    }

                    // 阻塞直到松手
                    if (tryAwaitRelease()) {
                        interactionSource.emit(PressInteraction.Release(press))
                    } else {
                        interactionSource.emit(PressInteraction.Cancel(press))
                    }

                    // 取消计时器
                    timer.cancel()
                    onRelease()
                },
                onTap = { onClick() },
                onLongPress = {}
            )
        }
}

/**
 * 用于在Modifier上添加条件启用的控制逻辑
 */
@SuppressLint("UnnecessaryComposedModifier")
fun Modifier.enableFor(
    enable: () -> Boolean,
    forFalse: @Composable Modifier.() -> Modifier = { this },
    forTrue: @Composable Modifier.() -> Modifier,
): Modifier = composed { if (enable()) this.forTrue() else this.forFalse() }