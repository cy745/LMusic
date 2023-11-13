package com.lalilu.component.extension

import android.util.LruCache
import androidx.palette.graphics.Palette
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult

/**
 * 利用Coil的Listener，结合Bitmap的generationId为Palette做缓存
 */
object PaletteFetcher {
    private val paletteCache = LruCache<String, Palette>(100)

    fun onSuccess(
        request: ImageRequest, result: SuccessResult, callback: (Palette) -> Unit
    ) {
        val cacheKey = result.memoryCacheKey ?: return
        val cacheBitmap = request.context.imageLoader.memoryCache?.get(cacheKey) ?: return
        val key = cacheBitmap.bitmap.generationId.toString()

        paletteCache.get(key).let {
            it ?: Palette.Builder(cacheBitmap.bitmap).generate()
                .apply { paletteCache.put(key, this) }
        }.let(callback)
    }
}

fun ImageRequest.Builder.requirePalette(callback: (Palette) -> Unit): ImageRequest.Builder {
    return allowHardware(false).listener(onSuccess = { request, result ->
        PaletteFetcher.onSuccess(request, result, callback)
    })
}