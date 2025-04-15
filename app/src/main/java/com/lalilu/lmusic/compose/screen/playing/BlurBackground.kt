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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.MediaItem
import androidx.palette.graphics.Palette
import com.github.panpf.sketch.asBitmapOrNull
import com.github.panpf.sketch.rememberAsyncImageState
import com.github.panpf.sketch.request.ComposableImageRequest
import com.github.panpf.sketch.request.LoadState
import com.lalilu.common.getAutomaticColor
import com.lalilu.lmusic.utils.StackBlurUtils
import com.lalilu.lmusic.utils.sketch.sketchUri

@Composable
fun BlurBackground(
    modifier: Modifier = Modifier,
    imageData: () -> Any,
    onBackgroundColorFetched: (Color) -> Unit,
    blurProgress: () -> Float,
) {
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
        val extraKey = remember { data.toString() }
        val samplingBitmap = remember { mutableStateOf<Bitmap?>(null) }
        val srcRect = remember { Rect() }
        val targetRect = remember { Rect() }

        val state = rememberAsyncImageState()
        val loadState: LoadState? = state.loadState

        LaunchedEffect(loadState) {
            if (loadState is LoadState.Success) {
                val temp = loadState.result.image.asBitmapOrNull()
                    ?: return@LaunchedEffect
                samplingBitmap.value = createSamplingBitmap(temp, 400).also {
                    // 提前预加载BlurredBitmap
                    StackBlurUtils.preload(it, extraKey)

                    if (it.width > it.height) {
                        val left = (it.width - it.height) / 2
                        val right = it.width - left
                        srcRect.set(left, 0, right, it.height)
                    } else {
                        val top = (it.height - it.width) / 2
                        val bottom = it.height - top
                        srcRect.set(0, top, it.width, bottom)
                    }
                }

                samplingBitmap.value?.let {
                    val color = Palette.from(it)
                        .generate()
                        .getAutomaticColor()
                    onBackgroundColorFetched(Color(color))
                }
            }
        }

        com.github.panpf.sketch.AsyncImage(
            modifier = Modifier
                .fillMaxSize()
                .drawWithContent {
                    val progress = blurProgress()
                    val radius = (progress * StackBlurUtils.MAX_RADIUS).toInt()

                    // 若无降采样图片或当前Radius为0则直接绘制原图
                    if (samplingBitmap.value == null || radius <= 0) {
                        this.drawContent()
                        return@drawWithContent
                    }

                    // 绘制 blurredBitmap
                    targetRect.set(0, 0, size.width.toInt(), size.height.toInt())
                    val blurredBitmap =
                        StackBlurUtils.processWithCache(samplingBitmap.value!!, radius, extraKey)
                    if (blurredBitmap != null) {
                        drawContext.canvas.nativeCanvas
                            .drawBitmap(blurredBitmap, srcRect, targetRect, null)
                    }

                    // 绘制 mask 透明的深色遮罩
                    maskPaint.alpha = (progress * 100f)
                        .coerceIn(0f, 255f)
                        .toInt()
                    drawContext.canvas.nativeCanvas.drawRect(targetRect, maskPaint)
                },
            request = ComposableImageRequest(
                uri = remember(imageData()) {
                    val data = imageData()
                    if (data is MediaItem) {
                        data.sketchUri().toString()
                    } else if (data is Int) {
                        "android.resource://resource?resId=$data"
                    } else {
                        null
                    }
                }
            ),
            contentDescription = null,
            state = state
        )
//        AsyncImage(
//            modifier = Modifier
//                .fillMaxSize()
//                .drawWithContent {
//                    val progress = blurProgress()
//                    val radius = (progress * StackBlurUtils.MAX_RADIUS).toInt()
//
//                    // 若无降采样图片或当前Radius为0则直接绘制原图
//                    if (samplingBitmap.value == null || radius <= 0) {
//                        this.drawContent()
//                        return@drawWithContent
//                    }
//
//                    // 绘制 blurredBitmap
//                    targetRect.set(0, 0, size.width.toInt(), size.height.toInt())
//                    val blurredBitmap =
//                        StackBlurUtils.processWithCache(samplingBitmap.value!!, radius, extraKey)
//                    if (blurredBitmap != null) {
//                        drawContext.canvas.nativeCanvas
//                            .drawBitmap(blurredBitmap, srcRect, targetRect, null)
//                    }
//
//                    // 绘制 mask 透明的深色遮罩
//                    maskPaint.alpha = (progress * 100f)
//                        .coerceIn(0f, 255f)
//                        .toInt()
//                    drawContext.canvas.nativeCanvas.drawRect(targetRect, maskPaint)
//                },
//            model = ImageRequest.Builder(context)
//                .data(data)
//                .allowHardware(false)
//                .build(),
//            contentScale = ContentScale.Crop,
//            contentDescription = "",
//            onSuccess = { state ->
//                val temp = state.result.image.toBitmap()
//                samplingBitmap.value = createSamplingBitmap(temp, 400).also {
//                    // 提前预加载BlurredBitmap
//                    StackBlurUtils.preload(it, extraKey)
//
//                    if (it.width > it.height) {
//                        val left = (it.width - it.height) / 2
//                        val right = it.width - left
//                        srcRect.set(left, 0, right, it.height)
//                    } else {
//                        val top = (it.height - it.width) / 2
//                        val bottom = it.height - top
//                        srcRect.set(0, top, it.width, bottom)
//                    }
//                }
//
//                samplingBitmap.value?.let {
//                    val color = Palette.from(it)
//                        .generate()
//                        .getAutomaticColor()
//                    onBackgroundColorFetched(Color(color))
//                }
//            }
//        )
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

    val scaleWidth: Float
    val scaleHeight: Float
    if (width > height) {
        scaleHeight = samplingValue.toFloat() / height
        scaleWidth = scaleHeight
    } else {
        scaleWidth = samplingValue.toFloat() / width
        scaleHeight = scaleWidth
    }
    matrix.setScale(scaleWidth, scaleHeight)

    return Bitmap.createBitmap(
        source, 0, 0, width, height, matrix, false
    )
}

