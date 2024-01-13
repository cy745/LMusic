package com.lalilu.lmusic.compose.screen.playing

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LyricSentence(
    lyric: LyricEntry,
    constraints: Constraints,
    textMeasurer: TextMeasurer,
    fontFamily: State<FontFamily?>,
    currentTime: () -> Long = { 0L },
    isTranslationShow: () -> Boolean = { false },
    isCurrent: () -> Boolean,
    onLongClick: () -> Unit = {},
    onClick: () -> Unit = {},
) {
    val density = LocalDensity.current
    val result = remember {
        textMeasurer.measure(
            text = lyric.text,
            constraints = constraints,
            style = TextStyle.Default.copy(
                fontSize = 26.sp,
                fontFamily = fontFamily.value
                    ?: TextStyle.Default.fontFamily
            )
        )
    }
    val translateResult = remember {
        lyric.translate?.let {
            textMeasurer.measure(
                text = it,
                constraints = constraints,
                style = TextStyle.Default.copy(
                    fontSize = 20.sp,
                    fontFamily = fontFamily.value
                        ?: TextStyle.Default.fontFamily
                )
            )
        }
    }
    val gapHeight = remember { with(density) { 10.dp.toPx() } }
    val textHeight = remember { result.getLineBottom(result.lineCount - 1) }
    val translateHeight = remember {
        translateResult?.let { it.getLineBottom(it.lineCount - 1) } ?: 0f
    }
    val height = remember { textHeight + translateHeight + gapHeight }
    val heightDp = remember { density.run { height.toDp() } }

    val color = animateColorAsState(
        targetValue = if (isCurrent()) Color.White else Color.Gray,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = ""
    )

    val scale = animateFloatAsState(
        targetValue = if (isCurrent()) 100f else 80f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = ""
    )

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(heightDp)
            .combinedClickable(onLongClick = onLongClick, onClick = onClick)
    ) {
        scale(
            scale = scale.value / 100f,
            pivot = Offset(x = 0f, y = height / 2f)
        ) {
            drawText(
                color = color.value,
                textLayoutResult = result,
            )
            translateResult?.let {
                drawText(
                    color = color.value,
                    topLeft = Offset.Zero.copy(y = textHeight + gapHeight),
                    textLayoutResult = it
                )
            }
        }
    }
}