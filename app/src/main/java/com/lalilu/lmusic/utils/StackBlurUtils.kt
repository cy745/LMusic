package com.lalilu.lmusic.utils

import android.graphics.Bitmap
import android.util.LruCache
import com.enrique.stackblur.NativeBlurProcess
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StackBlurUtils @Inject constructor() : NativeBlurProcess() {
    private val cache = LruCache<Int, Bitmap?>(50)
    private var sourceId = -1

    fun processWithCache(source: Bitmap?, radius: Int): Bitmap? {
        source ?: return null

        if (sourceId != source.generationId) {
            cache.evictAll()
        }
        sourceId = source.generationId
        return cache.get(radius) ?: blur(source, radius.toFloat()).also {
            cache.put(radius, it)
        }
    }
}