package com.lalilu.lmusic.utils

import android.graphics.Bitmap
import android.util.LruCache
import com.enrique.stackblur.NativeBlurProcess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

object StackBlurUtils : NativeBlurProcess(), CoroutineScope {
    const val MAX_RADIUS = 40

    private val cache = LruCache<String, Bitmap?>(MAX_RADIUS + 1)
    override val coroutineContext: CoroutineContext = Dispatchers.Default
    private var preloadJob: Job? = null

    fun evictAll() = cache.evictAll()

    fun processWithCache(
        source: Bitmap,
        radius: Int,
        extraKey: String = ""
    ): Bitmap? {
        val key = "${source.generationId}|$extraKey|$radius"
        return cache.get(key) ?: blur(source, radius.toFloat()).also { cache.put(key, it) }
    }

    fun preload(
        source: Bitmap,
        extraKey: String = ""
    ) {
        preloadJob?.cancel()
        preloadJob = launch {
            (1..MAX_RADIUS).map { radius ->
                async {
                    val key = "${source.generationId}|$extraKey|$radius"
                    if (cache.get(key) != null || !isActive) return@async

                    val temp = blur(source, radius.toFloat())
                    if (cache.get(key) != null || !isActive) return@async

                    cache.put(key, temp)
                }
            }.awaitAll()
        }
    }
}