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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
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
                    timer?.cancel()
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

fun Modifier.clipFade(
    cutting: Int = 10,
    lengthDp: Dp = 100.dp,
    alignmentX: Alignment.Horizontal? = null,
    alignmentY: Alignment.Vertical? = Alignment.Bottom,
    func: (x: Float) -> Float = { it * it }
) = composed {
    graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
        .drawWithCache {
            val alignment = alignmentX ?: alignmentY
            val length = lengthDp.toPx()
            val colorStops = (0..cutting step 1)
                .map { it / cutting.toFloat() }
                .map { it to Color.Black.copy(alpha = func(it)) }
                .toTypedArray()

            val (startValue, topLeft, drawSize) = when (alignment) {
                is Alignment.Vertical -> {
                    val startValue = size.height - length
                    val topLeft = Offset(x = 0.0F, y = startValue)
                    val drawSize = Size(width = size.width, height = length)

                    Triple(startValue, topLeft, drawSize)
                }

                is Alignment.Horizontal -> {
                    val startValue = size.width - length
                    val topLeft = Offset(x = startValue, y = 0f)
                    val drawSize = Size(width = length, height = size.height)

                    Triple(startValue, topLeft, drawSize)
                }

                else -> Triple(0f, Offset.Zero, size)
            }

            onDrawWithContent {
                drawContent()

                if (alignment is Alignment.Vertical) {
                    rotate(degrees = if (alignment == Alignment.Top) 180f else 0f) {
                        drawRect(
                            brush = Brush.verticalGradient(
                                colorStops = colorStops,
                                startY = startValue
                            ),
                            topLeft = topLeft,
                            size = drawSize,
                            blendMode = BlendMode.DstOut
                        )
                    }
                } else if (alignment is Alignment.Horizontal) {
                    rotate(degrees = if (alignment == Alignment.Start) 180f else 0f) {
                        drawRect(
                            brush = Brush.horizontalGradient(
                                colorStops = colorStops,
                                startX = startValue
                            ),
                            topLeft = topLeft,
                            size = drawSize,
                            blendMode = BlendMode.DstOut
                        )
                    }
                }
            }
        }
}