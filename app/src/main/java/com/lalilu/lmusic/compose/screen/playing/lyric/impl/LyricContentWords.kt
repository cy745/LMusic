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
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lalilu.lmedia.lyric.LyricItem
import com.lalilu.lmedia.lyric.findPlayingIndexForWords
import com.lalilu.lmedia.lyric.getSentenceContent
import com.lalilu.lmusic.compose.screen.playing.lyric.DEFAULT_TEXT_SHADOW
import com.lalilu.lmusic.compose.screen.playing.lyric.LyricContext
import com.lalilu.lmusic.compose.screen.playing.lyric.LyricSettings
import com.lalilu.lmusic.compose.screen.playing.lyric.utils.getPathForProgress
import com.lalilu.lmusic.compose.screen.playing.lyric.utils.normalized
import kotlin.math.abs


private val DEFAULT_GRADIENT_GAP = 48.dp

@Composable
fun LyricContentWords(
    index: Int,
    lyric: LyricItem.WordsLyric,
    modifier: Modifier = Modifier,
    settings: LyricSettings,
    context: LyricContext,
    onClick: (() -> Unit)?,
    onLongClick: (() -> Unit)?,
) {
    val density = LocalDensity.current
    val direction = LocalLayoutDirection.current
    val isCurrent = context.currentIndex() == index

    val fullSentence = remember { lyric.getSentenceContent() }
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

    val (textResult, translateResult) = remember(context, settings, lyric) {
        context.textMeasurer.measure(
            text = fullSentence,
            constraints = actualConstraints,
            style = settings.mainTextStyle
        ) to run {
            val text = lyric.translation.firstOrNull()?.content ?: return@run null
            context.textMeasurer.measure(
                text = text,
                constraints = actualConstraints,
                style = settings.translationTextStyle
            )
        }
    }

    val scale = animateFloatAsState(
        targetValue = when {
            isCurrent -> settings.scaleRange.endInclusive
            context.currentTime() in lyric.startTime..lyric.endTime -> 0.95f
            else -> settings.scaleRange.start
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
            isCurrent -> 1f
            context.currentTime() in lyric.startTime..lyric.endTime -> 0.75f
            else -> 0.5f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        ),
        visibilityThreshold = 0.001f,
        label = ""
    )

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

    val blurRadius = remember(
        context.isUserScrolling(),
        context.currentIndex(),
        settings.blurEffectEnable
    ) {
        if (context.isUserScrolling()) return@remember 0.dp
        if (!settings.blurEffectEnable) return@remember 0.dp
        abs(index - context.currentIndex()).coerceAtMost(5).dp
    }
    val animateBlurRadius = animateDpAsState(targetValue = blurRadius, label = "")

    Canvas(
        modifier = modifier
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
            .fillMaxWidth()
            .height(animateHeight.value)
            .combinedClickable(onLongClick = onLongClick, onClick = onClick ?: {})
            .blur(animateBlurRadius.value, BlurredEdgeTreatment.Unbounded) // TODO 对性能影响较大，待进一步优化
            .padding(settings.containerPadding)
    ) {
        val now = context.currentTime()
        val wordIndex = lyric.words.findPlayingIndexForWords(now)
        val word = lyric.words.getOrNull(wordIndex)

        // 获取某一词的播放进度
        var progress = normalized(
            start = word?.startTime ?: 0,
            end = word?.endTime ?: 0,
            current = now
        )

        // 若当前句的歌词已经播放完毕，则进度固定为1
        if (lyric.words.maxOf { it.endTime } < context.currentTime()) {
            progress = 1f
        }

        val offset = lyric.words.take(wordIndex)
            .sumOf { it.content.length }

        val (path, rect, position) = textResult.getPathForProgress(
            progress = progress,
            offset = offset,
            length = word?.content?.length
        )

        scale(
            scale = scale.value,
            pivot = pivotOffset as? Offset ?: Offset.Zero,
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
                topLeft = translationTopLeft as? Offset ?: Offset.Zero,
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