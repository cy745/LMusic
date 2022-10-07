package com.lalilu.lmusic.utils

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf

class SelectHelper<T>(
    private val isSelecting: MutableState<Boolean> = mutableStateOf(false)
) {
    val selectedItem = mutableStateListOf<T>()

    val isSelected: (T) -> Boolean = { selectedItem.contains(it) }

    val onSelected: (T) -> Unit = {
        isSelecting.value = true
        if (selectedItem.contains(it)) {
            selectedItem.remove(it)
        } else {
            selectedItem.add(it)
        }
    }

    val clear: () -> Unit = {
        isSelecting.value = false
        selectedItem.clear()
    }

    @Composable
    fun isSelecting(): MutableState<Boolean> {
        BackHandler(isSelecting.value) { clear() }
        return isSelecting
    }
}