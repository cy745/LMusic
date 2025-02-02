package com.lalilu.lmusic.compose.screen.playing.lyric.impl

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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.DeviceFontFamilyName
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lalilu.lmedia.lyric.LyricItem
import com.lalilu.lmedia.lyric.findPlayingIndexForWords
import com.lalilu.lmedia.lyric.getSentenceContent
import com.lalilu.lmusic.compose.screen.playing.lyric.utils.getPathForProgress
import com.lalilu.lmusic.compose.screen.playing.lyric.utils.normalized


private val DEFAULT_TEXT_SHADOW = Shadow(
    color = Color.Black.copy(alpha = 0.2f),
    offset = Offset(x = 0f, y = 1f),
    blurRadius = 1f
)

private val DEFAULT_GRADIENT_GAP = 48.dp

@Composable
fun LyricContentWords(
    modifier: Modifier,
    lyric: LyricItem.WordsLyric,
    isCurrent: () -> Boolean,
    offsetToCurrent: () -> Int,
    currentTime: () -> Long,
    screenConstraints: Constraints,
    onClick: (() -> Unit)?,
    onLongClick: (() -> Unit)?,
    textMeasurer: TextMeasurer,
    fontFamily: () -> FontFamily?
) {
    val density = LocalDensity.current
    val paddingVertical = remember { 15.dp }
    val paddingHorizontal = remember { 40.dp }
    val paddingVerticalPx = remember { with(density) { paddingVertical.roundToPx() } }
    val paddingHorizontalPx = remember { with(density) { paddingHorizontal.roundToPx() } }
    val gapHeight = remember(translationGap) { with(density) { translationGap.toPx() } }

    val fullSentence = remember { lyric.getSentenceContent() }
    val actualConstraints = remember {
        val width = screenConstraints.maxWidth - paddingHorizontalPx * 2
        Constraints(
            maxWidth = width,
            minWidth = width,
            maxHeight = Int.MAX_VALUE
        )
    }

    val textStyle = remember {
        TextStyle.Default.copy(
            fontSize = 34.sp,
            textAlign = TextAlign.Start,
            fontFamily = fontFamily() ?: FontFamily(
                Font(
                    familyName = DeviceFontFamilyName("FontFamily.Monospace"),
                    variationSettings = FontVariation.Settings(
                        FontVariation.weight(900)
                    )
                )
            )
        )
    }

    val textResult = remember {
        textMeasurer.measure(
            text = fullSentence,
            constraints = actualConstraints,
            style = textStyle
        )
    }

    val translateResult = remember {
        val text = lyric.translation.firstOrNull()?.content ?: return@remember null
        textMeasurer.measure(
            text = text,
            constraints = actualConstraints,
            style = textStyle.copy(fontSize = textStyle.fontSize * 0.7f)
        )
    }

    val scale = animateFloatAsState(
        targetValue = when {
            isCurrent() -> 100f
            currentTime() in lyric.startTime..lyric.endTime -> 95f
            else -> 90f
        },
        visibilityThreshold = 0.001f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = ""
    )
    val alpha = animateFloatAsState(
        targetValue = when {
            isCurrent() -> 1f
            currentTime() in lyric.startTime..lyric.endTime -> 0.75f
            else -> 0.5f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        ),
        visibilityThreshold = 0.001f,
        label = ""
    )

    val textHeight = remember(textResult) { textResult.getLineBottom(textResult.lineCount - 1) }
    val translateHeight = remember(translateResult) {
        translateResult?.let { it.getLineBottom(it.lineCount - 1) } ?: 0f
    }
    val pivotOffset = remember(textHeight) { Offset.Zero.copy(y = textHeight / 2f, x = 0f) }
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
    val translationTopLeft = remember(textHeight) {
        Offset.Zero.copy(y = textHeight + gapHeight)
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(animateHeight.value)
            .combinedClickable(onLongClick = onLongClick, onClick = onClick ?: {})
            .padding(vertical = paddingVertical, horizontal = paddingHorizontal)
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
    ) {
        val now = currentTime()
        val index = lyric.words.findPlayingIndexForWords(now)
        val word = lyric.words.getOrNull(index)

        // 获取某一词的播放进度
        var progress = normalized(
            start = word?.startTime ?: 0,
            end = word?.endTime ?: 0,
            current = now
        )

        // 若当前句的歌词已经播放完毕，则进度固定为1
        if (lyric.words.maxOf { it.endTime } < currentTime()) {
            progress = 1f
        }

        val offset = lyric.words.take(index)
            .sumOf { it.content.length }

        val (path, rect, position) = textResult.getPathForProgress(
            progress = progress,
            offset = offset,
            length = word?.content?.length
        )

        scale(
            scale = scale.value / 100f,
            pivot = pivotOffset
        ) {
            drawText(
                color = Color(0x80FFFFFF),
                shadow = DEFAULT_TEXT_SHADOW,
                textLayoutResult = textResult,
            )

            if (progress > 0f) {
                val lineProgress = if (progress >= 0.99f) 1f else {
                    normalized(
                        start = rect.left,
                        end = rect.right,
                        current = position
                    )
                }

                val offsetForProgress = DEFAULT_GRADIENT_GAP.toPx() * (1f - lineProgress)
                val leftBound = position - offsetForProgress
                val rightBound = (position + DEFAULT_GRADIENT_GAP.toPx() - offsetForProgress)
                val rectForGradient = rect.copy(left = leftBound, right = rightBound)

                // 向右扩展一段距离，为渐变预留足够的空间
                path.addRect(rectForGradient.copy(right = rectForGradient.right.coerceAtMost(rect.right)))

                clipPath(path) {
                    withLayer {
                        drawText(
                            color = Color.White,
                            textLayoutResult = textResult,
                        )

                        val gradient = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Black,
                                Color.Black.copy(0.4f),
                                Color.Transparent
                            ),
                            startX = leftBound,
                            endX = rightBound
                        )

                        clipPath(path = rect.toPath()) {
                            drawPath(
                                path = rectForGradient.toPath(),
                                brush = gradient,
                                blendMode = BlendMode.DstIn
                            )
                        }
                    }
                }
            }

            if (translateResult == null) return@scale
            drawText(
                color = Color(0x80FFFFFF),
                topLeft = translationTopLeft,
                shadow = DEFAULT_TEXT_SHADOW,
                textLayoutResult = translateResult,
            )
        }
    }
}

fun Rect.toPath(): Path {
    return Path().apply { addRect(this@toPath) }
}

fun DrawScope.withLayer(block: DrawScope.() -> Unit) {
    with(drawContext.canvas.nativeCanvas) {
        val layer = saveLayer(null, null)
        block()
        restoreToCount(layer)
    }
}