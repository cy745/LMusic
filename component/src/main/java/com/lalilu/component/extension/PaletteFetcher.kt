package com.lalilu.component.extension

import android.util.LruCache
import androidx.palette.graphics.Palette
import coil3.annotation.ExperimentalCoilApi
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.toBitmap

/**
 * 利用Coil的Listener，结合Bitmap的generationId为Palette做缓存
 */
object PaletteFetcher {
    private val paletteCache = LruCache<String, Palette>(100)

    @OptIn(ExperimentalCoilApi::class)
    fun onSuccess(
        request: ImageRequest, result: SuccessResult, callback: (Palette) -> Unit
    ) {
        val cacheKey = result.memoryCacheKey ?: return
        val cacheBitmap = request.context.imageLoader.memoryCache?.get(cacheKey) ?: return
        val bitmap = cacheBitmap.image.toBitmap()
        val key = bitmap.generationId.toString()

        paletteCache.get(key).let {
            it ?: Palette.Builder(bitmap).generate()
                .apply { paletteCache.put(key, this) }
        }.let(callback)
    }
}

fun ImageRequest.Builder.requirePalette(callback: (Palette) -> Unit): ImageRequest.Builder {
    return allowHardware(false).listener(onSuccess = { request, result ->
        PaletteFetcher.onSuccess(request, result, callback)
    })
}