package com.lalilu.common.kv

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

abstract class KVItem<T> : MutableState<T?>, ReadWriteProperty<KVItem<T>, T?>, UpdatableKV<T> {
    private var autoSave = false
    private val state: MutableState<T?> by lazy { mutableStateOf(null) }
    private val flowInternal: MutableStateFlow<T?> = MutableStateFlow(state.value)
    override var value: T?
        get() = state.value
        set(value) {
            val oldValue = state.value
            state.value = value
            if (oldValue != value && autoSave) {
                setInternal(value)
            }
        }

    override fun getValue(thisRef: KVItem<T>, property: KProperty<*>): T? = thisRef.value
    override fun setValue(thisRef: KVItem<T>, property: KProperty<*>, value: T?) =
        run { thisRef.value = value }

    override fun component1(): T? = value
    override fun component2(): (T?) -> Unit = { value = it }

    override fun save() = setInternal(value)
    override fun update() = run { value = get() }
    override fun flow(): Flow<T?> = flowInternal

    private fun setInternal(value: T?) {
        flowInternal.tryEmit(value)
        set(value)
    }
}