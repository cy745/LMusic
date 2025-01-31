package com.lalilu.lmusic.compose.screen.playing.lyric.utils

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.TextLayoutResult
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
): Path {
    val offsetWidth = getWidthForOffset(offset)
    val maxWidth = if (length == null) {
        sumWidthForLine(lineCount - 1)
    } else {
        getWidthForOffset(offset + length)
    }

    val targetWidth = offsetWidth + (maxWidth - offsetWidth) * progress
    var addedWidth = 0f

    val path = Path()
    for (lineIndex in 0 until lineCount) {
        val lineWidth = getLineWidth(lineIndex)

        // 若加上该行宽度会超出目标宽度，则说明该行已经超出目标宽度，此时需要截取该行
        if (addedWidth + lineWidth >= targetWidth) {
            val widthToAdd = targetWidth - addedWidth

            val rect = getLineRect(lineIndex)
                .let { it.copy(right = it.left + widthToAdd) }
            path.addRect(rect)
            addedWidth += widthToAdd
            break
        } else {
            val rect = getLineRect(lineIndex)
            path.addRect(rect)
            addedWidth += lineWidth
        }
    }

    return path
}