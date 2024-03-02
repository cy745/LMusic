package com.lalilu.lextension.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.lalilu.component.R as componentR


@Composable
fun ExtensionCard(
    modifier: Modifier = Modifier,
    draggableModifier: Modifier = Modifier,
    maskAlpha: Float = 0.5f,
    onUpClick: () -> Unit = {},
    onDownClick: () -> Unit = {},
    onVisibleChange: (Boolean) -> Unit = {},
    isVisible: () -> Boolean = { true },
    isEditing: () -> Boolean = { false },
    isDragging: () -> Boolean = { false },
    content: @Composable BoxScope.() -> Unit = {},
) {
    val visible = isVisible()
    val density = LocalDensity.current
    val heightDp = remember { mutableStateOf(0.dp) }
    val elevation = animateDpAsState(
        targetValue = if (isDragging()) 4.dp else 0.dp,
        label = ""
    )

    AnimatedVisibility(
        visible = isEditing() || visible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Surface(
            elevation = elevation.value,
            color = MaterialTheme.colors.background
        ) {
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .onSizeChanged { heightDp.value = density.run { it.height.toDp() } }
                    .run { if (isEditing()) this.heightIn(100.dp) else this }
                    .wrapContentHeight(),
                contentAlignment = Alignment.Center
            ) {
                content()

                AnimatedVisibility(
                    visible = isEditing(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .background(color = MaterialTheme.colors.surface.copy(alpha = maskAlpha))
                            .clickable(MutableInteractionSource(), indication = null) { }
                            .fillMaxWidth()
                            .height(heightDp.value),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End)
                        ) {
                            IconButton(onClick = { onVisibleChange(!visible) }) {
                                AnimatedContent(
                                    targetState = visible,
                                    label = ""
                                ) {
                                    val icon = if (it) {
                                        painterResource(id = componentR.drawable.ic_eye_off_fill)
                                    } else {
                                        painterResource(id = componentR.drawable.ic_edit_line)
                                    }

                                    Icon(
                                        modifier = Modifier.size(36.dp),
                                        painter = icon,
                                        contentDescription = ""
                                    )
                                }
                            }
                            IconButton(onClick = onUpClick) {
                                Icon(
                                    modifier = Modifier.size(36.dp),
                                    painter = painterResource(id = componentR.drawable.ic_arrow_up_s_line),
                                    contentDescription = ""
                                )
                            }
                            IconButton(onClick = onDownClick) {
                                Icon(
                                    modifier = Modifier.size(36.dp),
                                    painter = painterResource(id = componentR.drawable.ic_arrow_down_s_line),
                                    contentDescription = ""
                                )
                            }
                            Icon(
                                modifier = draggableModifier.size(36.dp),
                                painter = painterResource(id = componentR.drawable.ic_draggable),
                                contentDescription = ""
                            )
                        }
                    }
                }
            }
        }
    }
}