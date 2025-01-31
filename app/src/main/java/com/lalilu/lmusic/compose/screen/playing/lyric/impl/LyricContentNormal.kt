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
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lalilu.lmedia.lyric.LyricItem
import com.lalilu.lmusic.compose.screen.playing.lyric.LyricContent


data class LyricContentNormal(
    override val key: String,
    val lyric: LyricItem.NormalLyric,
) : LyricContent {
    override val item: LyricItem = lyric
    val translationGap: Dp = 10.dp
    val textAlign: TextAlign = TextAlign.Start
    val textSize: TextUnit = 26.sp
    val translationScale: Float = 0.8f
    val isTranslationShow: () -> Boolean = { true }
    val isBlurredEnable: () -> Boolean = { false }

    @Composable
    override fun Draw(
        modifier: Modifier,
        isCurrent: () -> Boolean,
        offsetToCurrent: () -> Int,
        currentTime: () -> Long,
        screenConstraints: Constraints,
        onClick: (() -> Unit)?,
        onLongClick: (() -> Unit)?,
        textMeasurer: TextMeasurer,
        fontFamily: () -> FontFamily?,
    ) {
        val density = LocalDensity.current
        val paddingVertical = remember { 15.dp }
        val paddingHorizontal = remember { 40.dp }
        val paddingVerticalPx = remember { with(density) { paddingVertical.roundToPx() } }
        val paddingHorizontalPx = remember { with(density) { paddingHorizontal.roundToPx() } }
        val gapHeight = remember(translationGap) { with(density) { translationGap.toPx() } }

        val actualConstraints = remember {
            val width = screenConstraints.maxWidth - paddingHorizontalPx * 2
            Constraints(
                maxWidth = width,
                minWidth = width,
                maxHeight = Int.MAX_VALUE
            )
        }
        val (textResult, translateResult) = remember(textAlign, textSize, fontFamily, lyric) {
            textMeasurer.measure(
                text = lyric.content,
                constraints = actualConstraints,
                style = TextStyle.Default.copy(
                    fontSize = textSize,
                    textAlign = textAlign,
                    fontFamily = fontFamily() ?: TextStyle.Default.fontFamily
                )
            ) to lyric.translation
                ?.takeIf(String::isNotBlank)
                ?.let {
                    textMeasurer.measure(
                        text = it,
                        constraints = actualConstraints,
                        style = TextStyle.Default.copy(
                            fontSize = textSize * translationScale,
                            textAlign = textAlign,
                            fontFamily = fontFamily() ?: TextStyle.Default.fontFamily
                        )
                    )
                }
        }

        val textHeight = remember(textResult) { textResult.getLineBottom(textResult.lineCount - 1) }
        val translateHeight = remember(translateResult) {
            translateResult?.let { it.getLineBottom(it.lineCount - 1) } ?: 0f
        }
        val height = remember(isTranslationShow(), textHeight, translateHeight) {
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
            targetValue = if (isCurrent()) 100f else 90f,
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
        val translationTopLeft = remember(textHeight) {
            Offset.Zero.copy(y = textHeight + gapHeight)
        }
        val pivotOffset = remember(height, textAlign) {
            val width = screenConstraints.maxWidth
            val x = when (textAlign) {
                TextAlign.End -> width.toFloat()
                TextAlign.Center -> width / 2f
                else -> 0f
            }
            Offset.Zero.copy(y = height / 2f, x = x)
        }
        val blurRadius = remember {
            derivedStateOf {
                if (!isBlurredEnable()) return@derivedStateOf 0.dp
                offsetToCurrent().coerceAtMost(5).dp
            }
        }
        val animateBlurRadius = animateDpAsState(targetValue = blurRadius.value, label = "")

        Canvas(
            modifier = modifier
                .blur(
                    animateBlurRadius.value,
                    BlurredEdgeTreatment.Unbounded
                ) // TODO 对性能影响较大，待进一步优化
                .fillMaxWidth()
                .height(animateHeight.value)
                .combinedClickable(onLongClick = onLongClick, onClick = onClick ?: {})
                .padding(vertical = paddingVertical, horizontal = paddingHorizontal)
        ) {
            scale(
                scale = scale.value / 100f,
                pivot = pivotOffset
            ) {
                drawText(
                    color = color.value,
                    shadow = textShadow,
                    textLayoutResult = textResult
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
}