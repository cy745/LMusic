package com.lalilu.component.extension

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

@Stable
class ItemSelector<T> {
    private val items = mutableStateOf(emptySet<T>())
    private val _isSelecting = mutableStateOf(false)
    val isSelecting: MutableState<Boolean> = object : MutableState<Boolean> {
        override var value: Boolean
            get() = _isSelecting.value
            set(value) = run { if (!value) clear(); _isSelecting.value = value }

        override fun component1(): Boolean = value
        override fun component2(): (Boolean) -> Unit = { value = it }
    }

    fun isSelected(item: T) = items.value.contains(item)
    fun selected() = items.value

    fun onSelect(item: T) {
        if (!isSelecting.value) isSelecting.value = true

        if (items.value.contains(item)) items.value -= item
        else items.value += item
    }

    fun selectAll(list: List<T>) {
        if (!isSelecting.value) isSelecting.value = true
        items.value = list.toSet()
    }

    fun clear() = run { items.value = emptySet() }
}

@Composable
fun <T> rememberSelector(): ItemSelector<T> {
    return remember { ItemSelector() }
}