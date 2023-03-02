package com.lalilu.lmusic.compose.component.base

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp

@Composable
fun IconTextButton(
    modifier: Modifier = Modifier,
    text: String,
    iconPainter: Painter? = null,
    shape: Shape = MaterialTheme.shapes.small,
    color: Color = MaterialTheme.colors.primary,
    showIcon: () -> Boolean = { false },
    onClick: () -> Unit = {}
) {
    TextButton(
        modifier = modifier,
        onClick = onClick,
        shape = shape,
        colors = ButtonDefaults.textButtonColors(
            contentColor = color,
            backgroundColor = color.copy(0.15f)
        )
    ) {
        AnimatedVisibility(visible = iconPainter != null && showIcon()) {
            Icon(
                modifier = Modifier
                    .padding(end = 5.dp)
                    .size(20.dp),
                painter = iconPainter!!,
                contentDescription = text,
            )
        }
        Text(text = text)
    }
}

@Composable
fun IconButton(
    modifier: Modifier = Modifier,
    icon: Painter,
    text: String,
    shape: Shape = MaterialTheme.shapes.small,
    color: Color = MaterialTheme.colors.primary,
    onClick: () -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }
    val buttonColor = ButtonDefaults.textButtonColors(
        contentColor = color,
        backgroundColor = color.copy(0.15f)
    )
//    var showText by remember { mutableStateOf(false) }
//    val haptic = LocalHapticFeedback.current

//    LaunchedEffect(Unit) {
//        var job: Job? = null
//        interactionSource.interactions.collect { interaction ->
//            if (interaction is PressInteraction.Press) {
//                job = launch {
//                    delay(1000)
//                    if (isActive) {
//                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
//                        showText = true
//                    }
//                }
//            } else {
//                job?.cancel()
//                showText = false
//            }
//        }
//    }

    TextButton(
        modifier = modifier,
        onClick = onClick,
        shape = shape,
        interactionSource = interactionSource,
        colors = buttonColor
    ) {
        Icon(
            modifier = Modifier.size(20.dp),
            painter = icon,
            contentDescription = text,
        )
    }
}