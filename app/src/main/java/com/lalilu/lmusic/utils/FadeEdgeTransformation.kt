package com.lalilu.lmusic.utils

import android.graphics.*
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.annotation.FloatRange
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.applyCanvas
import androidx.palette.graphics.Palette
import coil.size.Size
import coil.transform.Transformation

enum class EdgeDirection {
    TOP, BOTTOM, START, END
}

class FadeEdgeTransformation(
    direction: EdgeDirection = EdgeDirection.BOTTOM,
    @FloatRange(from = 0.0, to = 1.0) val percent: Float = 0.5f
) : Transformation {

    override val cacheKey: String = "${FadeEdgeTransformation::class.java.name}-$direction-$percent"
    private var palette: Palette? = null
    private var linearGradient: LinearGradient? = null

    val mPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
    }
    val rect = Rect()

    override suspend fun transform(input: Bitmap, size: Size): Bitmap {
        palette = palette ?: Palette.from(input).generate()
        linearGradient = linearGradient ?: LinearGradient(
            0f, 0f, 0f, 50f,
            xValue.map {
                Color(
                    palette!!.darkVibrantSwatch?.rgb ?: 0xFFFFFF
                ).copy(alpha = 1f - interpolator.getInterpolation(it)).toArgb()
            }.toIntArray(),
            xValue.toFloatArray(),
            Shader.TileMode.CLAMP
        )
        mPaint.shader = linearGradient
        rect.set(
            0,
            (input.width * percent).toInt(),
            input.width,
            input.height
        )
        val output = Bitmap.createBitmap(input.width, input.height, input.config)
        return output.applyCanvas {
            drawBitmap(input, 0f, 0f, null)
            drawRect(rect, mPaint)
        }
    }

    companion object {
        val xValue = (0..100 step 10).map { if (it == 0) 0f else it / 100f }
        val interpolator = AccelerateDecelerateInterpolator()
    }
}