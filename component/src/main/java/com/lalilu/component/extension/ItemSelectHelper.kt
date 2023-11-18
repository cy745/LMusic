package com.lalilu.component.extension

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color

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

/**
 * 定义针对选择器可执行的动作
 */
sealed interface SelectAction {
    data class StaticAction(
        @StringRes val title: Int,
        @DrawableRes val icon: Int? = null,
        @StringRes val info: Int? = null,
        val color: Color = Color.Transparent,
        val onAction: (ItemSelectHelper) -> Unit
    ) : SelectAction

    data class ComposeAction(
        val content: @Composable (ItemSelectHelper) -> Unit
    ) : SelectAction
}