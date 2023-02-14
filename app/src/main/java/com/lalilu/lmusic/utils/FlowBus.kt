package com.lalilu.lmusic.utils

import com.blankj.utilcode.util.LogUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


abstract class FlowBus {
    protected fun intBus() = BusItem<Int>()
    protected fun longBus() = BusItem<Long>()
    protected fun floatBus() = BusItem<Float>()
    protected fun boolBus() = BusItem<Boolean>()
    protected fun stringBus() = BusItem<String>()
    protected fun doubleBus() = BusItem<Double>()
    protected fun <T> objBus() = BusItem<T>()

    protected fun intStickBus() = StickBusItem<Int>()
    protected fun longStickBus() = StickBusItem<Long>()
    protected fun floatStickBus() = StickBusItem<Float>()
    protected fun boolStickBus() = StickBusItem<Boolean>()
    protected fun stringStickBus() = StickBusItem<String>()
    protected fun doubleStickBus() = StickBusItem<Double>()
    protected fun <T> objStickBus() = StickBusItem<T>()
}

open class BusItem<T> {
    private val _event: MutableSharedFlow<T> by lazy { obtainEvent() }

    open fun obtainEvent(): MutableSharedFlow<T> {
        return MutableSharedFlow(0, 1, BufferOverflow.DROP_OLDEST)
    }

    fun post(value: T) {
        _event.tryEmit(value)
    }

    fun post(scope: CoroutineScope, value: T) {
        scope.launch { _event.emit(value) }
    }

    fun register(scope: CoroutineScope, action: suspend (T) -> Unit) {
        scope.launch {
            _event.collectLatest {
                try {
                    action(it)
                } catch (e: Exception) {
                    LogUtils.i(e)
                }
            }
        }
    }

    fun flow(): Flow<T> = _event
}

open class StickBusItem<T> : BusItem<T>() {
    override fun obtainEvent(): MutableSharedFlow<T> {
        return MutableSharedFlow(1, 1, BufferOverflow.DROP_OLDEST)
    }
}