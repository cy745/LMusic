package com.lalilu.lmusic.utils

import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * 线程池工具类
 *
 */
class ThreadPoolUtils {
    companion object {
        fun newFixedThreadPool(numThread: Int): ThreadPoolExecutor {
            return ThreadPoolExecutor(
                numThread, numThread,
                0L,
                TimeUnit.MILLISECONDS,
                LinkedBlockingQueue()
            )
        }
    }
}