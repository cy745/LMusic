package com.lalilu.lmusic.utils

import android.graphics.Bitmap
import android.util.LruCache
import com.enrique.stackblur.NativeBlurProcess

object StackBlurUtils : NativeBlurProcess() {
    private val cache = LruCache<String, Bitmap?>(50)

    fun evictAll() = cache.evictAll()

    fun processWithCache(source: Bitmap, radius: Int): Bitmap? {
        return cache.get("${source.generationId}|$radius") ?: blur(source, radius.toFloat()).also {
            cache.put("${source.generationId}|$radius", it)
        }
    }
}