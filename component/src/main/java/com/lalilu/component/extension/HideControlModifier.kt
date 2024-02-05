package com.lalilu.component.extension

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive


fun Modifier.hideControl(
    enable: () -> Boolean,
    minAlpha: Float = 0f,
    maxAlpha: Float = 1f,
    hideDelay: Long = 3000L
) = composed {
    val isHide = remember(enable()) { mutableStateOf(enable()) }
    val showEvent = remember { mutableLongStateOf(-1L) }
    val animateAlpha = animateFloatAsState(
        targetValue = if (isHide.value) minAlpha * 100f else maxAlpha * 100f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = ""
    )

    LaunchedEffect(showEvent.longValue) {
        if (showEvent.longValue < 0) return@LaunchedEffect

        delay(hideDelay)

        // 若正显示则隐藏
        if (isActive && !isHide.value) {
            isHide.value = true
        }
    }

    this
        .alpha(animateAlpha.value / 100f)
        .enableFor(enable = enable) {
            pointerInput(Unit) {
                awaitPointerEventScope {
                    while (enable()) {
                        val event = awaitPointerEvent()

                        if (event.type == PointerEventType.Press) {
                            isHide.value = false
                        }

                        if (event.type == PointerEventType.Release) {
                            showEvent.longValue = System.currentTimeMillis()
                        }
                    }
                }
            }
        }
}