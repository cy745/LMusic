package com.lalilu.component

import androidx.compose.animation.core.AnimationSpec
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonColors
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Surface
import androidx.compose.material.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.lalilu.component.extension.longClickable


@Composable
fun LongClickableTextButton(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = RectangleShape,
    colors: ButtonColors = ButtonDefaults.textButtonColors(),
    border: BorderStroke? = null,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Center,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable RowScope.() -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var isClicking by remember { mutableStateOf(false) }

    ProgressTextButton(
        modifier = modifier
            .clip(shape)
            .longClickable(
                indication = ripple(color = Color.Transparent),
                onClick = { if (enabled) onClick() },
                enableHaptic = false,
                onLongClick = {},
                onTap = { isClicking = true },
                onRelease = { isClicking = false },
                interactionSource = interactionSource
            ),
        enabled = enabled,
        progress = { if (isClicking) 1f else 0f },
        shape = shape,
        colors = colors,
        border = border,
        contentPadding = contentPadding,
        horizontalArrangement = horizontalArrangement,
        onProgressFinished = {
            val skip = it != 1f || !enabled
            if (!skip) {
                isClicking = false
                onLongClick()
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
        },
        content = content
    )
}

@Composable
private fun ProgressTextButton(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    progress: () -> Float = { 1f },
    shape: Shape = remember { RoundedCornerShape(8.dp) },
    colors: ButtonColors = ButtonDefaults.textButtonColors(),
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    maskAnimationSpec: AnimationSpec<Float> = spring(
        Spring.DampingRatioNoBouncy,
        Spring.StiffnessVeryLow
    ),
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Center,
    onProgressFinished: ((Float) -> Unit)? = null,
    content: @Composable RowScope.() -> Unit
) {
    val contentColor by colors.contentColor(enabled)
    val maskColor = remember(contentColor) { contentColor.copy(alpha = 0.3f) }
    val maskWidthProgress by animateFloatAsState(
        label = "Animate mask width progress",
        targetValue = progress(),
        visibilityThreshold = 0.001f,
        animationSpec = maskAnimationSpec,
        finishedListener = onProgressFinished
    )

    Surface(
        modifier = modifier,
        shape = shape,
        color = colors.backgroundColor(enabled).value,
        contentColor = contentColor.copy(alpha = 1f),
        border = border,
        elevation = 0.dp
    ) {
        ProvideTextStyle(value = MaterialTheme.typography.button) {
            Row(
                Modifier
                    .drawBehind {
                        drawRect(
                            color = maskColor,
                            size = size.copy(width = size.width * maskWidthProgress)
                        )
                    }
                    .defaultMinSize(
                        minWidth = ButtonDefaults.MinWidth,
                        minHeight = ButtonDefaults.MinHeight
                    )
                    .padding(contentPadding),
                horizontalArrangement = horizontalArrangement,
                verticalAlignment = Alignment.CenterVertically,
                content = content
            )
        }
    }
}