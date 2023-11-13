package com.lalilu.lmusic.compose.component.base

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.IconToggleButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
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
    val pressedState = interactionSource.collectIsPressedAsState()
    val iconColor by animateColorAsState(
        targetValue = if (isChecked) checkedColor else dayNightTextColor(0.3f),
        label = ""
    )
    val scaleValue by animateFloatAsState(
        animationSpec = SpringSpec(dampingRatio = Spring.DampingRatioMediumBouncy),
        targetValue = if (pressedState.value) 1.1f else 1f,
        label = ""
    )

    Surface(
        modifier = modifier.scale(scaleValue),
        shape = shape,
        color = iconColor.copy(0.15f)
    ) {
        IconToggleButton(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            interactionSource = interactionSource
        ) {
            Icon(
                painter = painterResource(id = if (isChecked) checkedIconRes else normalIconRes),
                tint = iconColor,
                contentDescription = "A Checkable Button"
            )
        }
    }
}