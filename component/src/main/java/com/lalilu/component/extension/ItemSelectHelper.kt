package com.lalilu.component.extension

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

data class ItemSelectHelper(
    val isSelecting: MutableState<Boolean>,
    val selected: MutableState<List<Any>>
) {
    fun isSelecting() = isSelecting.value

    fun onSelect(item: Any) {
        if (!isSelecting.value) isSelecting.value = true

        selected.value = if (selected.value.contains(item)) {
            selected.value.minus(item)
        } else {
            selected.value.plus(item)
        }
    }

    fun isSelected(item: Any): Boolean {
        return selected.value.contains(item)
    }

    fun clear() {
        selected.value = emptyList()
        isSelecting.value = false
    }
}

@Composable
fun rememberItemSelectHelper(
    isSelecting: MutableState<Boolean> = remember { mutableStateOf(false) },
    selected: MutableState<List<Any>> = remember { mutableStateOf(emptyList()) }
): ItemSelectHelper {
    return remember {
        ItemSelectHelper(
            isSelecting = isSelecting,
            selected = selected
        )
    }
}