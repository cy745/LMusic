package com.lalilu.common

import kotlin.coroutines.cancellation.CancellationException

/**
 * 协程同步器，通过持有一个count，并使用这个count与同步器进行比对，
 * 若不相同则说明该任务已被新任务替代，抛出异常取消该旧任务
 */
class CoroutineSynchronizer {
    @Volatile
    private var counter: Long = 0L

    fun getCount(): Long = synchronized(this::class) {
        counter += 1
        counter %= 10000
        counter
    }

    /**
     * 判断所持有的count是否与最新的count相同，
     * 不相同则抛出 [CancellationException] 结束该协程作用域，从而使协程内任务结束
     *
     * suspend 标注必须在协程作用域内使用，否则无任何效果
     */
    suspend fun checkCount(count: Long) = synchronized(this::class) {
        if (count != counter) throw CancellationException("Coroutine Job has been cancelled.")
    }
}