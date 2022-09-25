package com.lalilu.lmusic.utils

import kotlin.coroutines.cancellation.CancellationException

/**
 * 协程同步器，通过持有一个count，并使用这个count与同步器进行比对，
 * 若不相同则说明该任务已新任务替代，抛出异常取消该任务
 */
class CoroutineSynchronizer {
    @Volatile
    private var counter: Long = 0L

    suspend fun getCount(): Long = synchronized(this::class) {
        counter += 1
        counter
    }

    suspend fun checkCount(count: Long) = synchronized(this::class) {
        if (count != counter) throw CancellationException("Coroutine Job has been cancelled.")
    }
}