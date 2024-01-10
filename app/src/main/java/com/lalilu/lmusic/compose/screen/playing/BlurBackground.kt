package com.lalilu.lmusic.compose.screen.playing

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.palette.graphics.Palette
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.lalilu.common.getAutomaticColor
import com.lalilu.lmusic.utils.StackBlurUtils
import com.lalilu.lmusic.utils.extension.toBitmap

@Composable
fun BlurBackground(
    modifier: Modifier = Modifier,
    imageData: () -> Any,
    onBackgroundColorFetched: (Color) -> Unit,
    blurProgress: () -> Float,
) {
    val progress = rememberUpdatedState(newValue = blurProgress())
    val context = LocalContext.current
    val maskPaint =
        remember { Paint(Paint.ANTI_ALIAS_FLAG).also { it.color = android.graphics.Color.BLACK } }

    AnimatedContent(
        label = "",
        modifier = modifier,
        targetState = imageData(),
        transitionSpec = {
            fadeIn(tween(500)) togetherWith fadeOut(tween(300, 500))
        }
    ) { data ->
        val samplingBitmap = remember { mutableStateOf<Bitmap?>(null) }
        val targetRect = remember { Rect() }

        SubcomposeAsyncImage(
            modifier = Modifier
                .fillMaxSize()
                .drawWithContent {
                    val radius = (progress.value * 50f).toInt()

                    if (samplingBitmap.value == null || radius == 0) {
                        this.drawContent()
                    } else {
                        val top = ((size.height - size.width) / 2f).toInt()
                        val bottom = (size.height - top).toInt()
                        targetRect.set(0, top, size.width.toInt(), bottom)

                        StackBlurUtils
                            .processWithCache(samplingBitmap.value!!, radius)
                            ?.let {
                                drawContext.canvas.nativeCanvas.drawBitmap(
                                    it, null, targetRect, null
                                )
                            }

                        maskPaint.alpha = (progress.value * 100f)
                            .coerceIn(0f, 255f)
                            .toInt()
                        drawContext.canvas.nativeCanvas.drawRect(targetRect, maskPaint)
                    }
                },
            model = ImageRequest.Builder(context)
                .data(data)
                .allowHardware(false)
                .build(),
            contentScale = ContentScale.FillWidth,
            contentDescription = "",
            onSuccess = {
                val temp = it.result.drawable.toBitmap()
                val samplingTemp = createSamplingBitmap(temp, 400)

                println("samplingTemp: ${samplingTemp.width} ${samplingTemp.height}")
                samplingBitmap.value = samplingTemp

                val color = Palette.from(samplingTemp)
                    .generate()
                    .getAutomaticColor()
                onBackgroundColorFetched(Color(color))
            }
        )
    }
}

/**
 * 重采样图片，降低图片大小，用于Blur
 *
 * @param source 源图
 * @param samplingValue 输出图片的最大边大小
 * @return 经过重采样的Bitmap
 */
fun createSamplingBitmap(source: Bitmap, samplingValue: Int): Bitmap {
    val width = source.width
    val height = source.height
    val matrix = Matrix()

    val scaleWidth = samplingValue.toFloat() / width
    val scaleHeight = samplingValue.toFloat() / height
    matrix.postScale(scaleWidth, scaleHeight)

    return Bitmap.createBitmap(
        source, 0, 0, width, height, matrix, false
    )
}

