package com.lalilu.lmusic.utils

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList

class SelectHelper<T>(
    defaultState: Boolean = false,
    private val onExitSelect: () -> Unit = {},
    private val selectedItems: SnapshotStateList<T>
) {
    val isSelecting = mutableStateOf(defaultState)

    fun clear() {
        isSelecting.value = false
        selectedItems.clear()
        onExitSelect()
    }

    fun onSelected(item: T) {
        if (!isSelecting.value) {
            isSelecting.value = true
        }

        if (selectedItems.contains(item)) {
            selectedItems.remove(item)
        } else {
            selectedItems.add(item)
        }
    }

    @Composable
    fun registerBackHandler() {
        BackHandler(isSelecting.value) {
            clear()
        }
    }
}

@Composable
fun <T> rememberSelectState(
    defaultState: Boolean = false,
    selectedItems: SnapshotStateList<T> = mutableStateListOf(),
    onExitSelect: () -> Unit = {}
): SelectHelper<T> {
    return remember {
        SelectHelper(
            defaultState = defaultState,
            onExitSelect = onExitSelect,
            selectedItems = selectedItems
        )
    }.also {
        it.registerBackHandler()
    }
}