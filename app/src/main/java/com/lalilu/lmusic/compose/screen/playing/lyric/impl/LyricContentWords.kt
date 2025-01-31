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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lalilu.lmedia.lyric.LyricItem
import com.lalilu.lmedia.lyric.findPlayingIndexForWords
import com.lalilu.lmedia.lyric.getSentenceContent
import com.lalilu.lmusic.compose.screen.playing.lyric.LyricContent
import com.lalilu.lmusic.compose.screen.playing.lyric.utils.getPathForProgress
import com.lalilu.lmusic.compose.screen.playing.lyric.utils.normalized

class LyricContentWords(
    override val key: String,
    val lyric: LyricItem.WordsLyric,
) : LyricContent {
    override val item: LyricItem = lyric

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
        fontFamily: () -> FontFamily?
    ) {
        val density = LocalDensity.current
        val paddingVertical = remember { 15.dp }
        val paddingHorizontal = remember { 40.dp }
        val paddingVerticalPx = remember { with(density) { paddingVertical.roundToPx() } }
        val paddingHorizontalPx = remember { with(density) { paddingHorizontal.roundToPx() } }

        val actualConstraints = remember {
            val width = screenConstraints.maxWidth - paddingHorizontalPx * 2
            Constraints(
                maxWidth = width,
                minWidth = width,
                maxHeight = Int.MAX_VALUE
            )
        }

        val textResult = remember {
            textMeasurer.measure(
                text = lyric.getSentenceContent(),
                constraints = actualConstraints,
                style = TextStyle.Default.copy(
                    fontSize = 26.sp,
                    textAlign = TextAlign.Start,
                    fontFamily = fontFamily() ?: TextStyle.Default.fontFamily
                )
            )
        }
        val scale = animateFloatAsState(
            targetValue = if (isCurrent()) 100f else 90f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessLow
            ),
            label = ""
        )
        val textHeight = remember(textResult) { textResult.getLineBottom(textResult.lineCount - 1) }
        val heightDp =
            remember(textHeight) { density.run { textHeight.toDp() + paddingVertical * 2 } }
        val animateHeight = animateDpAsState(
            targetValue = heightDp,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow
            ),
            label = ""
        )
        val pivotOffset = remember(textHeight) {
            Offset.Zero.copy(y = textHeight / 2f, x = 0f)
        }
        val textShadow = remember {
            Shadow(
                color = Color.Black.copy(alpha = 0.2f),
                offset = Offset(x = 0f, y = 1f),
                blurRadius = 1f
            )
        }

        Canvas(
            modifier = modifier
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
                    color = Color(0x80FFFFFF),
                    shadow = textShadow,
                    textLayoutResult = textResult
                )

//                val progress = normalized(
//                    start = lyric.startTime,
//                    end = lyric.endTime,
//                    current = currentTime()
//                )

                if (isCurrent()) {
                    val index = lyric.words.findPlayingIndexForWords(currentTime())
                    val word = lyric.words.getOrNull(index) ?: return@scale
                    val progress = normalized(
                        start = word.startTime,
                        end = word.endTime,
                        current = currentTime()
                    )
                    val offset = lyric.words.take(index)
                        .sumOf { it.content.length }

                    val path = textResult.getPathForProgress(
                        progress = progress,
                        offset = offset,
                        length = word.content.length
                    )

                    clipPath(
                        path = path
                    ) {
                        drawText(
                            color = Color(0xFFFFFFFF),
                            shadow = textShadow,
                            textLayoutResult = textResult
                        )
                    }
                }
            }
        }
    }
}

