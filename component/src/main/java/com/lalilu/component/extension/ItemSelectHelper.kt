package com.lalilu.component.extension

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.lalilu.component.R

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
    sealed class StaticAction(
        @StringRes open val title: Int,
        @DrawableRes open val icon: Int? = null,
        @StringRes open val info: Int? = null,
        open val color: Color = Color.Transparent,
        open val onAction: (ItemSelectHelper) -> Unit
    ) : SelectAction {
        data class SelectAll(val getAll: () -> List<Any>) : StaticAction(
            title = R.string.select_action_title_select_all,
            color = Color(0xFF1F5AC0),
            onAction = { helper ->
                val all = getAll()
                val diff = all.any { !helper.selected.value.contains(it) }
                helper.selected.value = if (diff) all else emptyList()
            }
        )

        data object ClearAll : StaticAction(
            title = R.string.select_action_title_clear_all,
            color = Color(0xFFDF9323),
            onAction = { it.selected.value = emptyList() }
        )

        data class Custom(
            override val title: Int,
            override val icon: Int? = null,
            override val info: Int? = null,
            override val color: Color = Color.Transparent,
            override val onAction: (ItemSelectHelper) -> Unit
        ) : StaticAction(title, icon, info, color, onAction)
    }

    data class ComposeAction(
        val content: @Composable (ItemSelectHelper) -> Unit
    ) : SelectAction
}