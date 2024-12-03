package com.lalilu.component.smartbar.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import com.lalilu.RemixIcon
import com.lalilu.remixicon.System
import com.lalilu.remixicon.system.moreLine
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.collections.indexOf


@Composable
internal fun MoreActionBtn(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colors.onBackground,
    dotColors: List<Color> = emptyList(),
    onClick: () -> Unit = {}
) {
    TextButton(
        modifier = modifier.fillMaxHeight(),
        shape = RectangleShape,
        colors = ButtonDefaults.textButtonColors(
            backgroundColor = color.copy(alpha = 0.15f),
            contentColor = color
        ),
        onClick = onClick
    ) {
        val showingColor = remember { mutableStateOf<Color?>(null) }

        LaunchedEffect(showingColor.value) {
            if (showingColor.value == null) {
                showingColor.value = dotColors.firstOrNull()
                return@LaunchedEffect
            }

            delay(3000)
            if (!isActive) return@LaunchedEffect

            val currentIndex = dotColors.indexOf(showingColor.value)
            val nextIndex = (currentIndex + 1) % dotColors.size
            showingColor.value = dotColors.getOrNull(nextIndex)
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                modifier = Modifier.size(20.dp),
                imageVector = RemixIcon.System.moreLine,
                contentDescription = null,
                tint = color
            )

            showingColor.value?.let { dotColor ->
                AnimatedContent(
                    modifier = Modifier.align(Alignment.TopStart),
                    transitionSpec = {
                        fadeIn(spring(stiffness = Spring.StiffnessLow)) togetherWith
                                fadeOut(spring(stiffness = Spring.StiffnessLow))
                    },
                    targetState = dotColor,
                    label = ""
                ) {
                    Spacer(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(color = it)
                            .size(8.dp)
                    )
                }
            }
        }
    }
}
