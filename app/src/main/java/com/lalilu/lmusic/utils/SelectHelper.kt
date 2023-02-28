package com.lalilu.lmusic.utils

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.lalilu.lmusic.compose.component.SmartModalBottomSheet

class SelectHelper<T>(
    defaultState: Boolean = false,
    private val onExitSelect: () -> Unit = {}
) {
    val isSelecting = mutableStateOf(defaultState)
    val selectedItems: SnapshotStateList<T> = mutableStateListOf()

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
    fun RegisterBackHandler() {
        BackHandler(isSelecting.value && SmartModalBottomSheet.isVisible.value) {
            clear()
        }
    }
}

@Composable
fun <T> rememberSelectState(
    defaultState: Boolean = false,
    onExitSelect: () -> Unit = {}
): SelectHelper<T> {
    return remember {
        SelectHelper<T>(
            defaultState = defaultState,
            onExitSelect = onExitSelect
        )
    }.also {
        it.RegisterBackHandler()
    }
}