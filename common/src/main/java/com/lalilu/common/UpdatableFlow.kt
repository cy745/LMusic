package com.lalilu.common

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest

/**
 * 将[Flow]转为可手动更新并将更新下传至其他[Flow]的[UpdatableFlow]
 */
@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class UpdatableFlow<T>(
    private val flow: Flow<T>,
    debouncingInterval: Long = 0
) : Flow<T> {
    private val currentTimeFlow = MutableStateFlow(-1L)
    private val rootFlow = currentTimeFlow
        .debounce { value -> if (value == -1L) 0 else debouncingInterval }
        .flatMapLatest { flow }

    override suspend fun collect(collector: FlowCollector<T>) {
        rootFlow.collect(collector)
    }

    fun requireUpdate() {
        currentTimeFlow.tryEmit(System.currentTimeMillis())
    }
}

fun <T> Flow<T>.toUpdatableFlow(
    debouncingInterval: Long = 0
): UpdatableFlow<T> {
    return UpdatableFlow(this, debouncingInterval)
}