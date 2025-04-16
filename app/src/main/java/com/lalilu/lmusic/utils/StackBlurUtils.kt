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
    override val coroutineContext: CoroutineContext = Dispatchers.IO

    const val MAX_RADIUS = 40
    private val cache = object : LruCache<String, Bitmap>(50 * 1024 * 1024) {
        override fun sizeOf(key: String?, value: Bitmap?): Int {
            return value?.byteCount ?: 0
        }

        override fun entryRemoved(
            evicted: Boolean,
            key: String?,
            oldValue: Bitmap?,
            newValue: Bitmap?
        ) {
            runCatching {
                if (oldValue?.isRecycled == false) {
                    oldValue.recycle()
                }
            }
        }
    }
    private var preloadJob: Job? = null


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