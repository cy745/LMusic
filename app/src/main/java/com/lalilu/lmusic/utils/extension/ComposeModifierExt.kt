package com.lalilu.lmusic.utils.extension

import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Shader
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lalilu.component.extension.rememberFixedStatusBarHeight

/**
 * 在布局元素时获取其测量好的宽高并通过callback返回给调用方
 */
fun Modifier.measure(callback: (width: Int, height: Int) -> Unit): Modifier =
    this.layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        callback.invoke(placeable.measuredWidth, placeable.measuredHeight)
        layout(constraints.maxWidth, placeable.measuredHeight) {
            placeable.place(0, 0)
        }
    }

fun Modifier.measureHeight(callback: (width: Int, height: Int) -> Unit): Modifier =
    this.layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        callback.invoke(placeable.measuredWidth, placeable.measuredHeight)
        layout(placeable.measuredWidth, placeable.measuredHeight) {
            placeable.place(0, 0)
        }
    }

fun Modifier.heightWithNavigateBar(): Modifier = composed {
    val height = WindowInsets.navigationBars
        .asPaddingValues()
        .calculateBottomPadding() + LocalConfiguration.current.screenHeightDp.dp
    this.height(height)
}

fun Modifier.navigateBarHeight(multiple: Float = 1f): Modifier = composed {
    this.height(
        WindowInsets.navigationBars
            .asPaddingValues()
            .calculateBottomPadding() * multiple
    )
}

fun Modifier.edgeTransparentForStatusBar(enable: Boolean = true) = composed {
    if (enable) {
        edgeTransparent(top = rememberFixedStatusBarHeight().toFloat())
    } else {
        Modifier
    }
}

fun Modifier.edgeTransparent(
    position: Int = EDGE_TOP,
    edgeWidth: Dp = 0.dp,
    percent: Float = 0f
) = composed {
    LocalDensity.current.run { edgeTransparent(position, edgeWidth.toPx(), percent) }
}

const val EDGE_TOP = 0x01
const val EDGE_BOTTOM = EDGE_TOP shl 1
const val EDGE_LEFT = EDGE_TOP shl 2
const val EDGE_RIGHT = EDGE_TOP shl 3

/**
 * 学习了 https://github.com/qinci/EdgeTranslucent 的边缘模糊化过渡实现
 * 将其转为Compose适用的方法
 *
 * @param position 所要透明过渡的边缘
 * @param edgeWidth 希望在元素的顶边进行模糊化的边缘宽度
 */
fun Modifier.edgeTransparent(
    position: Int,
    edgeWidth: Float,
    percent: Float = 0f,
    interpolator: (x: Float) -> Float = AccelerateDecelerateInterpolator()::getInterpolation
): Modifier = composed {
    val xValue = (0..100 step 10).map { if (it == 0) 0f else it / 100f }
    val mPaint: Paint = remember {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
            if (edgeWidth > 0f) {
                shader = LinearGradient(
                    0f, 0f, 0f, edgeWidth,
                    xValue.map { Color.White.copy(alpha = 1f - interpolator(it)).toArgb() }
                        .toIntArray(),
                    xValue.toFloatArray(), Shader.TileMode.CLAMP
                )
            }
        }
    }

    var actualWidth: Float
    var layerSaveTemp: Int
    var layerSave: Int
    var canvas: Canvas

    this.drawWithContent {
        canvas = drawContext.canvas.nativeCanvas
        layerSave = canvas.saveLayer(0f, 0f, size.width, size.height, null)
        drawContent()

        actualWidth = if (percent > 0f) size.height * percent else edgeWidth

        if (mPaint.shader == null || edgeWidth <= 0f) {
            mPaint.shader = LinearGradient(
                0f, 0f, 0f, actualWidth,
                xValue.map { Color.White.copy(alpha = 1f - interpolator(it)).toArgb() }
                    .toIntArray(),
                xValue.toFloatArray(), Shader.TileMode.CLAMP
            )
        }

        if (position == 0 || (position and EDGE_TOP) != 0) {
            canvas.drawRect(0f, 0f, size.width, actualWidth, mPaint)
        }

        if (position == 0 || (position and EDGE_BOTTOM) != 0) {
            layerSaveTemp = canvas.save()
            canvas.rotate(180f, size.width / 2f, size.height / 2f)
            canvas.drawRect(0f, 0f, size.width, actualWidth, mPaint)
            canvas.restoreToCount(layerSaveTemp)
        }

        canvas.restoreToCount(layerSave)
    }
}

private val interpolator: (x: Float) -> Float = AccelerateDecelerateInterpolator()::getInterpolation
private val xValue = (0..100 step 10).map { if (it == 0) 0f else it / 100f }.toFloatArray()
private val colorArray =
    xValue.map { it to Color.Black.copy(alpha = 1f - interpolator(it)) }.toTypedArray()

fun Modifier.edgeTransparent(
    left: Dp = 0.dp,
    top: Dp = 0.dp,
    right: Dp = 0.dp,
    bottom: Dp = 0.dp
): Modifier = composed {
    val density = LocalDensity.current
    val leftPx = remember { with(density) { left.toPx() } }
    val topPx = remember { with(density) { top.toPx() } }
    val rightPx = remember { with(density) { right.toPx() } }
    val bottomPx = remember { with(density) { bottom.toPx() } }
    if (leftPx == 0f && topPx == 0f && rightPx == 0f && bottomPx == 0f) return@composed this

    return@composed edgeTransparent(leftPx, topPx, rightPx, bottomPx)
}

fun Modifier.edgeTransparent(
    left: Float = 0f,
    top: Float = 0f,
    right: Float = 0f,
    bottom: Float = 0f
): Modifier = composed {
    if (left == 0f && top == 0f && right == 0f && bottom == 0f) return@composed this

    var topBrush: Brush? = remember { null }
    var bottomBrush: Brush? = remember { null }
    var leftBrush: Brush? = remember { null }
    var rightBrush: Brush? = remember { null }
    val blendMode = remember { BlendMode.DstOut }

    var layerSave: Int
    var canvas: Canvas

    drawWithContent {
        canvas = drawContext.canvas.nativeCanvas
        layerSave = canvas.saveLayer(0f, 0f, size.width, size.height, null)

        this@drawWithContent.drawContent()
        if (top > 0) {
            if (topBrush == null) {
                topBrush = Brush.linearGradient(
                    colorStops = colorArray,
                    start = Offset.Zero,
                    end = Offset.Zero.copy(y = top),
                    tileMode = TileMode.Clamp
                )
            }
            drawRect(
                brush = topBrush!!,
                size = size.copy(height = top),
                blendMode = blendMode
            )
        }

        if (bottom > 0) {
            rotate(180f) {
                if (bottomBrush == null) {
                    bottomBrush = Brush.linearGradient(
                        colorStops = colorArray,
                        start = Offset.Zero,
                        end = Offset.Zero.copy(y = bottom),
                        tileMode = TileMode.Clamp
                    )
                }

                drawRect(
                    brush = bottomBrush!!,
                    size = size.copy(height = bottom),
                    blendMode = blendMode
                )
            }
        }

        if (left > 0) {
            if (leftBrush == null) {
                leftBrush = Brush.linearGradient(
                    colorStops = colorArray,
                    start = Offset.Zero,
                    end = Offset.Zero.copy(x = left),
                    tileMode = TileMode.Clamp
                )
            }

            drawRect(
                brush = leftBrush!!,
                size = size.copy(width = left),
                blendMode = blendMode
            )
        }

        if (right > 0) {
            rotate(180f) {
                if (rightBrush == null) {
                    rightBrush = Brush.linearGradient(
                        colorStops = colorArray,
                        start = Offset.Zero,
                        end = Offset.Zero.copy(x = right),
                        tileMode = TileMode.Clamp
                    )
                }

                drawRect(
                    brush = rightBrush!!,
                    size = size.copy(width = right),
                    blendMode = blendMode
                )
            }
        }

        canvas.restoreToCount(layerSave)
    }
}
