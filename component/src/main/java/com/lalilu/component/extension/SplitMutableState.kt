package com.lalilu.component.extension

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlin.reflect.KProperty

private class SettingListenState<T>(
    private val defaultValue: T,
    private val onSetValue: (T) -> Unit = {},
    private val instance: MutableState<T> = mutableStateOf(defaultValue),
) : MutableState<T> {
    override var value: T
        get() = instance.value
        set(value) {
            if (instance.value != value) {
                instance.value = value
                onSetValue(value)
            }
        }

    override fun component1(): T = this.value
    override fun component2(): (T) -> Unit = { this.value = it }
    operator fun setValue(thisObj: Any?, property: KProperty<*>, v: T) = run { value = v }
    operator fun getValue(thisObj: Any?, property: KProperty<*>): T = this.value
}

interface Transform<T, K> {
    fun to(value: T): K
    fun from(item: K): T
}

@Composable
fun <T, K, V> MutableState<T>.split(
    getValue: (T) -> K,
    setValue: MutableState<T>.(K) -> T,
    transform: Transform<K, V>
): MutableState<V> {
    return remember {
        SettingListenState(
            defaultValue = transform.to(getValue(this.value)),
            onSetValue = { this.value = setValue(transform.from(it)) }
        )
    }.also { it.value = transform.to(getValue(this.value)) }
}

@Composable
fun <T, K> MutableState<T>.split(
    getValue: (T) -> K,
    onSetValue: MutableState<T>.(K) -> T,
): MutableState<K> {
    return remember {
        SettingListenState(
            defaultValue = getValue(this.value),
            onSetValue = { this.value = onSetValue(it) }
        )
    }.also { it.value = getValue(this.value) }
}

@Composable
fun <T, K> transform(
    to: (value: T) -> K,
    from: (item: K) -> T
): Transform<T, K> {
    return remember {
        object : Transform<T, K> {
            override fun to(value: T): K = to(value)
            override fun from(item: K): T = from(item)
        }
    }
}