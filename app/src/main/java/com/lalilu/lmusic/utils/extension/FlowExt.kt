package com.lalilu.lmusic.utils.extension

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * 将[Flow]转为可手动更新并将更新下传至其他[Flow]的[UpdatableFlow]
 */
@OptIn(ExperimentalCoroutinesApi::class)
class UpdatableFlow<T>(private val flow: Flow<T>) : Flow<T> {
    private val currentTimeFlow = MutableStateFlow(System.currentTimeMillis())
    private val rootFlow = currentTimeFlow.flatMapLatest { flow }

    override suspend fun collect(collector: FlowCollector<T>) {
        rootFlow.collect(collector)
    }

    fun requireUpdate() {
        currentTimeFlow.tryEmit(System.currentTimeMillis())
    }
}

fun <T> Flow<T>.toUpdatableFlow(): UpdatableFlow<T> {
    return UpdatableFlow(this)
}


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

/**
 * 将Flow转换为State
 */
fun <T> Flow<T>.toState(scope: CoroutineScope): State<T?> {
    return mutableStateOf<T?>(null).also { state ->
        this.onEach { state.value = it }.launchIn(scope)
    }
}

/**
 * 将Flow转换为State，附带初始值
 */
fun <T> Flow<T>.toState(defaultValue: T, scope: CoroutineScope): State<T> {
    return mutableStateOf(defaultValue).also { state ->
        this.onEach { state.value = it }.launchIn(scope)
    }
}

/**
 * 将Flow转换为MutableState
 */
fun <T> Flow<T>.toMutableState(scope: CoroutineScope): MutableState<T?> {
    return mutableStateOf<T?>(null).also { state ->
        this.onEach { state.value = it }.launchIn(scope)
    }
}


/**
 * 将Flow转换为MutableState
 */
fun <T> Flow<T>.toMutableState(defaultValue: T, scope: CoroutineScope): MutableState<T> {
    return mutableStateOf(defaultValue).also { state ->
        this.onEach { state.value = it }.launchIn(scope)
    }
}

fun <T> Flow<T>.collectWithLifeCycleOwner(
    lifecycleOwner: LifecycleOwner,
    withState: Lifecycle.State = Lifecycle.State.STARTED,
    callback: suspend (T) -> Unit,
) {
    lifecycleOwner.lifecycleScope.launch {
        lifecycleOwner.lifecycle.repeatOnLifecycle(withState) {
            this@collectWithLifeCycleOwner.collect(callback)
        }
    }
}