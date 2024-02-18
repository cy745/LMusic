package com.lalilu.component.extension

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive


/**
 * 隐藏控制，触摸时显示元素，手指离开后延时隐藏
 *
 * @param enable        是否启动隐藏逻辑
 * @param intercept     是否拦截第一次点击的显示事件（点击一次后显示元素，再次点击即触发元素内的点击事件）
 * @param minAlpha      最小透明度
 * @param maxAlpha      最大透明度
 * @param hideDelay     隐藏延时
 */
@OptIn(ExperimentalComposeUiApi::class)
fun Modifier.hideControl(
    enable: () -> Boolean,
    intercept: () -> Boolean = { false },
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

    this.alpha(animateAlpha.value / 100f)
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
        .enableFor(enable = { isHide.value && intercept() }) {
            pointerInteropFilter { true }
        }
}