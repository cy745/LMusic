package com.lalilu.lmusic.compose.screen.playing.lyric.utils

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Dp
import kotlin.math.abs

/**
 * 获取指定行的宽度
 */
fun TextLayoutResult.getLineWidth(lineIndex: Int): Float {
    return abs(getLineRight(lineIndex) - getLineLeft(lineIndex))
}

/**
 * 获取指定行的矩形
 */
fun TextLayoutResult.getLineRect(lineIndex: Int): Rect {
    return Rect(
        left = getLineLeft(lineIndex),
        right = getLineRight(lineIndex),
        top = getLineTop(lineIndex),
        bottom = getLineBottom(lineIndex)
    )
}

/**
 * 获取指定行与其之前的所有行的宽度之和
 */
fun TextLayoutResult.sumWidthForLine(lineIndex: Int): Int {
    if (lineIndex < 0) return 0
    return (0..lineIndex).sumOf { getLineWidth(it).toInt() }
}

/**
 * 获取指定字符偏移值对应的宽度
 */
fun TextLayoutResult.getWidthForOffset(offset: Int): Int {
    val lineIndex = getLineForOffset(offset)
    val position = getHorizontalPosition(offset, true).toInt()
    return sumWidthForLine(lineIndex - 1) + position
}

@Immutable
data class WordsLayoutResult(
    @Stable val path: Path,
    @Stable val rect: Rect,
    @Stable val position: Float
)

/**
 * 获取指定进度对应的路径
 *
 * @param progress  进度
 * @param offset    起始偏移值
 * @param length    长度
 */
fun TextLayoutResult.getPathForProgress(
    progress: Float,
    offset: Int = 0,
    length: Int? = null
): WordsLayoutResult {
    val offsetWidth = getWidthForOffset(offset)
    val maxWidth = if (length == null) {
        sumWidthForLine(lineCount - 1)
    } else {
        getWidthForOffset(offset + length)
    }

    val targetWidth = offsetWidth + (maxWidth - offsetWidth) * progress
    var addedWidth = 0f

    val path = Path()
    var rect: Rect? = null
    var position = 0f
    for (lineIndex in 0 until lineCount) {
        val lineWidth = getLineWidth(lineIndex)

        // 若加上该行宽度会超出目标宽度，则说明该行已经超出目标宽度，此时需要截取该行
        if (addedWidth + lineWidth >= targetWidth) {
            val widthToAdd = targetWidth - addedWidth

            val lineRect = getLineRect(lineIndex)
            val lineRectWithProgress = lineRect.let { it.copy(right = it.left + widthToAdd) }
            path.addRect(lineRectWithProgress)

            // 获取当前行（词）的左右边界
            rect = lineRect.copy(
                left = lineRect.left + offsetWidth - addedWidth,
                right = lineRect.left + maxWidth - addedWidth
            )

            // 获取当前行（词）的播放位置
            position = lineRectWithProgress.right
            addedWidth += widthToAdd
            break
        } else {
            val lineRect = getLineRect(lineIndex)
            path.addRect(lineRect)
            position = lineRect.right
            addedWidth += lineWidth
            rect = lineRect
        }
    }

    return WordsLayoutResult(
        path = path,
        rect = rect ?: Rect.Zero,
        position = position
    )
}

fun normalized(start: Long, end: Long, current: Long): Float {
    if (start >= end) return 0f
    val result = (current - start).toFloat() / (end - start).toFloat()
    return result.coerceIn(0f, 1f)
}

fun normalized(start: Float, end: Float, current: Float): Float {
    if (start >= end) return 0f
    val result = (current - start) / (end - start)
    return result.coerceIn(0f, 1f)
}

private val blurEffectMap = mutableMapOf<Int, BlurEffect>()
internal fun Modifier.blur(radius: () -> Dp) = graphicsLayer {
    val px = radius().roundToPx()
    this.renderEffect =
        if (px > 0f) blurEffectMap.getOrPut(px) {
            BlurEffect(
                px.toFloat(),
                px.toFloat(),
                TileMode.Decal
            )
        }
        else null
    this.clip = false
}