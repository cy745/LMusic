package com.lalilu.lmusic.compose.screen.playing.lyric.impl

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lalilu.lmedia.lyric.LyricItem
import com.lalilu.lmusic.compose.screen.playing.lyric.DEFAULT_TEXT_SHADOW
import com.lalilu.lmusic.compose.screen.playing.lyric.LyricContext
import com.lalilu.lmusic.compose.screen.playing.lyric.LyricSettings
import kotlin.math.abs


@Composable
fun LyricContentNormal(
    index: Int,
    lyric: LyricItem.NormalLyric,
    modifier: Modifier = Modifier,
    settings: LyricSettings,
    context: LyricContext,
    onClick: (() -> Unit)?,
    onLongClick: (() -> Unit)?,
) {
    val density = LocalDensity.current
    val direction = LocalLayoutDirection.current
    val isCurrent = context.currentIndex() == index

    val actualConstraints = remember(context, settings) {
        val paddingHorizontal = settings.containerPadding.calculateLeftPadding(direction) +
                settings.containerPadding.calculateRightPadding(direction)
        val paddingHorizontalPx = with(density) { paddingHorizontal.roundToPx() }
        val width = context.screenConstraints.maxWidth - paddingHorizontalPx
        Constraints(
            maxWidth = width,
            minWidth = width,
            maxHeight = Int.MAX_VALUE
        )
    }
    val (textResult, translateResult) = remember(settings, context, lyric) {
        context.textMeasurer.measure(
            text = lyric.content,
            constraints = actualConstraints,
            style = settings.mainTextStyle
        ) to lyric.translation
            ?.takeIf(String::isNotBlank)
            ?.let {
                context.textMeasurer.measure(
                    text = it,
                    constraints = actualConstraints,
                    style = settings.translationTextStyle
                )
            }
    }

    val (heightDp, translationTopLeft, pivotOffset) = remember(
        textResult, translateResult, settings
    ) {
        val gapHeight = with(density) { settings.gapSize.toPx() }
        val textHeight = textResult.getLineBottom(textResult.lineCount - 1)
        val translateHeight = translateResult?.let { it.getLineBottom(it.lineCount - 1) } ?: 0f

        val height =
            if (settings.translationVisible && translateHeight > 0) textHeight + translateHeight + gapHeight
            else textHeight
        val paddingVertical = settings.containerPadding.calculateTopPadding() +
                settings.containerPadding.calculateBottomPadding()

        val width = context.screenConstraints.maxWidth
        val x = when (settings.textAlign) {
            TextAlign.End -> width.toFloat()
            TextAlign.Center -> width / 2f
            else -> 0f
        }
        val pivotOffset = Offset.Zero.copy(y = height / 2f, x = x)

        listOf(
            density.run { height.toDp() + paddingVertical },
            Offset.Zero.copy(y = textHeight + gapHeight),
            pivotOffset
        )
    }

    val animateHeight = animateDpAsState(
        targetValue = heightDp as? Dp ?: 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = ""
    )

    val animateAlpha = animateFloatAsState(
        targetValue = if (settings.translationVisible) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = ""
    )

    val color = animateColorAsState(
        targetValue = if (isCurrent) Color.White else Color(0x80FFFFFF),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = ""
    )

    val scale = animateFloatAsState(
        targetValue = if (isCurrent) settings.scaleRange.endInclusive
        else settings.scaleRange.start,
        visibilityThreshold = 0.001f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = ""
    )

    val blurRadius = remember {
        derivedStateOf {
            if (context.isUserScrolling()) return@derivedStateOf 0.dp
            if (!settings.blurEffectEnable) return@derivedStateOf 0.dp
            abs(index - context.currentIndex()).coerceAtMost(5).dp
        }
    }
    val animateBlurRadius = animateDpAsState(targetValue = blurRadius.value, label = "")

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(animateHeight.value)
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
            .blur(animateBlurRadius.value, BlurredEdgeTreatment.Unbounded) // TODO 对性能影响较大，待进一步优化
            .combinedClickable(onLongClick = onLongClick, onClick = onClick ?: {})
            .padding(settings.containerPadding)
    ) {
        scale(
            scale = scale.value,
            pivot = pivotOffset as? Offset ?: Offset.Zero
        ) {
            drawText(
                color = color.value,
                shadow = DEFAULT_TEXT_SHADOW,
                textLayoutResult = textResult
            )

            if (translateResult == null) return@scale
            drawText(
                color = color.value,
                topLeft = translationTopLeft as? Offset ?: Offset.Zero,
                textLayoutResult = translateResult,
                alpha = animateAlpha.value
            )
        }
    }
}