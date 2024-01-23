package com.lalilu.lmusic.compose.screen.playing

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
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
    modifier: Modifier = Modifier,
    lyric: LyricEntry,
    textMeasurer: TextMeasurer,
    fontFamily: State<FontFamily?>,
    maxWidth: () -> Int = { 1080 },
    currentTime: () -> Long = { 0L },
    isTranslationShow: () -> Boolean = { false },
    isCurrent: () -> Boolean,
    onLongClick: () -> Unit = {},
    onClick: () -> Unit = {},
) {
    val density = LocalDensity.current
    val paddingVertical = remember { 15.dp }
    val paddingHorizontal = remember { 40.dp }
    val paddingVerticalPx = remember { with(density) { paddingVertical.roundToPx() } }
    val paddingHorizontalPx = remember { with(density) { paddingHorizontal.roundToPx() } }
    val gapHeight = remember { with(density) { 10.dp.toPx() } }

    val actualConstraints = remember {
        Constraints(maxWidth = maxWidth() - paddingHorizontalPx * 2, maxHeight = Int.MAX_VALUE)
    }
    val result = remember {
        textMeasurer.measure(
            text = lyric.text,
            constraints = actualConstraints,
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
                constraints = actualConstraints,
                style = TextStyle.Default.copy(
                    fontSize = 20.sp,
                    fontFamily = fontFamily.value
                        ?: TextStyle.Default.fontFamily
                )
            )
        }
    }
    val textHeight = remember { result.getLineBottom(result.lineCount - 1) }
    val translateHeight = remember {
        translateResult?.let { it.getLineBottom(it.lineCount - 1) } ?: 0f
    }
    val height = remember(isTranslationShow()) {
        textHeight + if (isTranslationShow() && translateHeight > 0) translateHeight + gapHeight else 0f
    }
    val heightDp = remember(height) { density.run { height.toDp() + paddingVertical * 2 } }
    val animateHeight = animateDpAsState(
        targetValue = heightDp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = ""
    )

    val animateAlpha = animateFloatAsState(
        targetValue = if (isTranslationShow()) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = ""
    )

    val color = animateColorAsState(
        targetValue = if (isCurrent()) Color.White else Color(0x80FFFFFF),
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

    val textShadow = remember {
        Shadow(
            color = Color.Black.copy(alpha = 0.2f),
            offset = Offset(x = 0f, y = 1f),
            blurRadius = 1f
        )
    }
    val translationTopLeft = remember {
        Offset.Zero.copy(y = textHeight + gapHeight)
    }
    val pivotOffset = remember(height) {
        Offset.Zero.copy(y = height / 2f)
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(animateHeight.value)
            .combinedClickable(onLongClick = onLongClick, onClick = onClick)
            .padding(vertical = paddingVertical, horizontal = paddingHorizontal)
    ) {
        scale(
            scale = scale.value / 100f,
            pivot = pivotOffset
        ) {
            drawText(
                color = color.value,
                shadow = textShadow,
                textLayoutResult = result
            )

            if (translateResult == null) return@scale
            drawText(
                color = color.value,
                topLeft = translationTopLeft,
                textLayoutResult = translateResult,
                alpha = animateAlpha.value
            )
        }
    }
}