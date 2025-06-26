package com.lalilu.lmusic.compose.screen.playing.lyric.impl

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TransformOrigin
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
import androidx.compose.ui.unit.dp
import com.lalilu.lmedia.lyric.LyricItem
import com.lalilu.lmedia.lyric.findPlayingIndexForWords
import com.lalilu.lmedia.lyric.getSentenceContent
import com.lalilu.lmusic.compose.screen.playing.lyric.DEFAULT_TEXT_SHADOW
import com.lalilu.lmusic.compose.screen.playing.lyric.LyricContext
import com.lalilu.lmusic.compose.screen.playing.lyric.LyricSettings
import com.lalilu.lmusic.compose.screen.playing.lyric.utils.blur
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

    val textResult = remember(context, settings, lyric) {
        context.textMeasurer.measure(
            text = fullSentence,
            constraints = actualConstraints,
            style = settings.mainTextStyle
        )
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
    val pivot = remember(settings.textAlign) {
        when (settings.textAlign) {
            TextAlign.End -> TransformOrigin.Center.copy(pivotFractionX = 1f)
            TextAlign.Center -> TransformOrigin.Center
            else -> TransformOrigin.Center.copy(pivotFractionX = 0f)
        }
    }
    val blurRadius = remember(
        context.isUserScrolling(),
        context.currentIndex(),
        settings.blurEffectEnable
    ) {
        if (context.isUserScrolling()) return@remember 0.dp
        if (!settings.blurEffectEnable) return@remember 0.dp
        abs(index - context.currentIndex()).times(3).coerceAtMost(10).dp
    }
    val animateBlurRadius = animateDpAsState(
        targetValue = blurRadius,
        label = ""
    )
    val translationVisible = remember(settings, lyric, isCurrent) {
        if (!settings.translationVisible) return@remember false
        if (lyric.translation.isEmpty()) return@remember false
        if (lyric.translation.firstOrNull()?.content?.isBlank() == true) return@remember false
        if (settings.onlyCurrentTranslationVisible && !isCurrent) return@remember false

        return@remember true
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .blur { animateBlurRadius.value }
            .combinedClickable(onLongClick = onLongClick, onClick = onClick ?: {})
            .padding(settings.containerPadding)
            .graphicsLayer {
                clip = false
                compositingStrategy = CompositingStrategy.Offscreen
                transformOrigin = pivot
                scaleX = scale.value
                scaleY = scaleX
            },
    ) {
        Canvas(
            modifier = modifier
                .fillMaxWidth()
                .height(density.run { textResult.getLineBottom(textResult.lineCount - 1).toDp() })
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

            // 若当前词已经播放完毕，则进度固定为1
            if ((word?.endTime ?: Long.MAX_VALUE) < now) {
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
                pivot = center.copy(x = pivot.pivotFractionX * size.width),
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
                    path.addRect(
                        rectForGradient.copy(
                            right = rectForGradient.right.coerceAtMost(
                                rect.right
                            )
                        )
                    )

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
            }
        }

        if (lyric.translation.isNotEmpty()) {
            AnimatedVisibility(
                modifier = Modifier.fillMaxWidth(),
                visible = translationVisible,
                enter = fadeIn() + expandVertically(clip = false),
                exit = fadeOut() + shrinkVertically(clip = false)
            ) {
                val animateAlpha = transition.animateFloat {
                    when (it) {
                        EnterExitState.PreEnter -> 0f
                        EnterExitState.Visible -> 1f
                        EnterExitState.PostExit -> 0f
                    }
                }
                Text(
                    text = lyric.translation[0].content,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = settings.gapSize)
                        .graphicsLayer { alpha = animateAlpha.value },
                    style = settings.translationTextStyle,
                    color = Color(0x80FFFFFF)
                )
            }
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