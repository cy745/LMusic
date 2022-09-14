package com.lalilu.lmusic.utils.extension

import android.graphics.*
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

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
        edgeTransparent(
            position = EDGE_TOP,
            edgeWidth = WindowInsets.statusBars.getTop(LocalDensity.current).toFloat()
        )
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
