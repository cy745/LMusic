package com.lalilu.lmusic.utils

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.mapLatest

@OptIn(ExperimentalCoroutinesApi::class)
class CachedFlow<T>(flow: Flow<T>) : Flow<T> {
    @Volatile
    private var cache: T? = null
    private val rootFlow = flow.mapLatest { it.also { cache = it } }

    override suspend fun collect(collector: FlowCollector<T>) {
        rootFlow.collect(collector)
    }

    fun get(): T? = cache
}

fun <T> Flow<T>.toCachedFlow(): CachedFlow<T> {
    return CachedFlow(this)
}