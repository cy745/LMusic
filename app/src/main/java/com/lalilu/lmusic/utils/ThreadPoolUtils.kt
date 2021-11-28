package com.lalilu.lmusic.utils

import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * 线程池工具类
 *
 */
object ThreadPoolUtils {
    fun newFixedThreadPool(numThread: Int): ThreadPoolExecutor {
        return ThreadPoolExecutor(
            numThread, numThread,
            0L,
            TimeUnit.MILLISECONDS,
            LinkedBlockingQueue()
        )
    }

    object CachedThreadPool : ThreadPoolExecutor(
        0, Int.MAX_VALUE,
        60L,
        TimeUnit.SECONDS,
        SynchronousQueue()
    )

    @Deprecated("后期删除，需让线程池保持单例模式")
    fun newCachedThreadPool(): ThreadPoolExecutor {
        return ThreadPoolExecutor(
            0, Int.MAX_VALUE,
            60L,
            TimeUnit.SECONDS,
            SynchronousQueue()
        )
    }
}