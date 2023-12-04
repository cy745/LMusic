package com.lalilu.lmusic.compose.component.base

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import com.lalilu.component.extension.dayNightTextColor

@Composable
fun IconCheckButton(
    modifier: Modifier = Modifier,
    shape: Shape = CircleShape,
    checkedColor: Color = MaterialTheme.colors.primary,
    @DrawableRes checkedIconRes: Int,
    @DrawableRes normalIconRes: Int,
    getIsChecked: () -> Boolean,
    onCheckedChange: (Boolean) -> Unit = {},
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    val isChecked = getIsChecked()
    val haptic = LocalHapticFeedback.current
    val pressedState = interactionSource.collectIsPressedAsState()
    val iconColor by animateColorAsState(
        targetValue = if (isChecked) checkedColor else dayNightTextColor(0.3f),
        label = ""
    )
    val scaleValue by animateFloatAsState(
        animationSpec = SpringSpec(dampingRatio = Spring.DampingRatioMediumBouncy),
        targetValue = if (pressedState.value) 1.2f else 1f,
        label = ""
    )

    Surface(
        modifier = modifier,
        shape = shape,
        color = iconColor.copy(0.15f)
    ) {
        Box(
            modifier = modifier
                .minimumInteractiveComponentSize()
                .toggleable(
                    value = isChecked,
                    onValueChange = {
                        onCheckedChange(it)
                        if (it) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                    },
                    role = Role.Checkbox,
                    interactionSource = interactionSource,
                    indication = null
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                modifier = Modifier.scale(scaleValue),
                painter = painterResource(id = if (isChecked) checkedIconRes else normalIconRes),
                tint = iconColor,
                contentDescription = "A Checkable Button"
            )
        }
    }
}