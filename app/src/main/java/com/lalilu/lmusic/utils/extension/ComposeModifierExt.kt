package com.lalilu.lmusic.utils.extension

import android.graphics.*
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
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

fun Modifier.edgeTransparent(top: Dp) = composed {
    edgeTransparent(LocalDensity.current.run { top.toPx() })
}

/**
 * 学习了 https://github.com/qinci/EdgeTranslucent 的边缘模糊化过渡实现
 * 将其转为Compose适用的方法
 *
 * @param top 希望在元素的顶边进行模糊化的边缘宽度
 */
fun Modifier.edgeTransparent(top: Float): Modifier = composed {
    val interpolator = AccelerateDecelerateInterpolator()
    val xValue = (0..100 step 10).map { if (it == 0) 0f else it / 100f }
    val mPaint = remember(top) {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
            shader = LinearGradient(
                0f, 0f, 0f, top,
                xValue.map {
                    Color.White.copy(alpha = 1f - interpolator.getInterpolation(it)).toArgb()
                }.toIntArray(),
                xValue.toFloatArray(),
                Shader.TileMode.CLAMP
            )
        }
    }
    var layerSave: Int
    var canvas: Canvas
    this.drawWithContent {
        canvas = drawContext.canvas.nativeCanvas
        layerSave = canvas.saveLayer(0f, 0f, size.width, size.height, null)
        drawContent()
        canvas.drawRect(0f, 0f, size.width, top, mPaint)
        canvas.restoreToCount(layerSave)
    }
}
