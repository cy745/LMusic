package com.lalilu.lmusic.utils.coil

import android.graphics.Bitmap
import android.util.LruCache
import androidx.palette.graphics.Palette
import coil.size.Size
import coil.transform.Transformation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 利用Coil的Transformation机制，异步提取Bitmap的颜色
 *
 * @param callback 异步回调，返回一个Palette对象
 */
class PaletteTransformation(
    private val callback: suspend (Palette) -> Unit
) : Transformation {
    override val cacheKey: String = "Palette"

    override suspend fun transform(input: Bitmap, size: Size): Bitmap {
        withContext(Dispatchers.Default) {
            callback(Palette.Builder(input).generate())
        }
        return input
    }

    companion object {
        val resultCache = LruCache<String, Palette>(100)
    }
}