package com.lalilu.lmusic.utils

import android.graphics.Bitmap
import android.util.LruCache
import com.enrique.stackblur.NativeBlurProcess

object StackBlurUtils : NativeBlurProcess() {
    private val cache = LruCache<String, Bitmap?>(50)
    private var sourceId = -1

    fun evictAll() = cache.evictAll()

    fun processWithCache(source: Bitmap?, radius: Int): Bitmap? {
        source ?: return null

        sourceId = source.generationId
        return cache.get("$sourceId$radius") ?: blur(source, radius.toFloat()).also {
            cache.put("$sourceId$radius", it)
        }
    }
}