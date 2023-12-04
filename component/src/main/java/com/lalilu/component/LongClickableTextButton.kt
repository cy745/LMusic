package com.lalilu.component

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ButtonColors
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.lalilu.component.extension.longClickable


@Composable
fun LongClickableTextButton(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape,
    colors: ButtonColors,
    border: BorderStroke? = null,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    enableLongClickMask: Boolean = false,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable RowScope.() -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var isClicking by remember { mutableStateOf(false) }
    val contentColor by colors.contentColor(enabled)
    val maskColor = remember { contentColor.copy(alpha = 0.3f) }
    val maskWidthProgress by animateFloatAsState(
        label = "Animate mask width progress",
        targetValue = if (isClicking) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessVeryLow
        ),
        finishedListener = {
            if (it != 1f || !enableLongClickMask || !enabled) return@animateFloatAsState

            isClicking = false
            onLongClick()
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    )

    Surface(
        modifier = modifier
            .longClickable(
                onClick = { if (enabled) onClick() },
                enableHaptic = false,
                onLongClick = {},
                onTap = { isClicking = true },
                onRelease = { isClicking = false },
                interactionSource = interactionSource
            ),
        shape = shape,
        color = colors.backgroundColor(enabled).value,
        contentColor = contentColor.copy(alpha = 1f),
        border = border,
        elevation = 0.dp
    ) {
        ProvideTextStyle(value = MaterialTheme.typography.button) {
            Row(
                Modifier
                    .run {
                        if (!enableLongClickMask) return@run this
                        drawBehind {
                            drawRect(
                                color = maskColor,
                                size = size.copy(width = size.width * maskWidthProgress)
                            )
                        }
                    }
                    .defaultMinSize(
                        minWidth = ButtonDefaults.MinWidth,
                        minHeight = ButtonDefaults.MinHeight
                    )
                    .padding(contentPadding),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                content = content
            )
        }
    }
}