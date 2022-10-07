package com.lalilu.lmusic.utils

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.*

class SelectHelper<T> {
    private val isSelecting = mutableStateOf(false)
    val selectedItem = mutableStateListOf<T>()

    val isSelected: (T) -> Boolean = {
        if (!isSelecting.value) clear()
        selectedItem.contains(it)
    }

    val clear: () -> Unit = {
        isSelecting.value = false
        selectedItem.clear()
    }

    val onSelected: (T) -> Unit = {
        isSelecting.value = true
        if (selectedItem.contains(it)) selectedItem.remove(it) else selectedItem.add(it)
    }

    @Composable
    fun listenIsSelectingChange(onChange: (Boolean) -> Unit) {
        LaunchedEffect(isSelecting.value) {
            onChange(isSelecting.value)

            if (!isSelecting.value) clear()
        }
    }

    @Composable
    fun onSelected(callback: (T) -> Unit): (T) -> Unit {
        return remember {
            {
                if (isSelecting.value) onSelected(it) else callback(it)
            }
        }
    }

    @Composable
    fun registerBackHandler() {
        BackHandler(isSelecting.value) { clear() }
    }
}