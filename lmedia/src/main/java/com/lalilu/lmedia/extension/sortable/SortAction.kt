package com.lalilu.lmedia.extension.sortable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.painter.Painter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest
import java.io.Serializable

@Stable
data class ActionInfo(
    val title: String,
    val subTitle: String? = null,
    val icon: Painter? = null
)

interface SortAction : Serializable {
    fun key(): String? = null

    @Stable
    @Composable
    fun getActionInfo(): ActionInfo = ActionInfo("")

    @OptIn(ExperimentalCoroutinesApi::class)
    fun <T : Sortable> doSort(
        items: Flow<List<T>>,
        config: SortConfig = SortConfig()
    ): Flow<SortResult<T>> = items
        .mapLatest { doSortInternal(it, config) }
        .mapLatest { if (config.hideGroup) SortResult.flat(it.itemList) else it }

    fun <T : Sortable> doSortInternal(
        items: List<T>,
        config: SortConfig = SortConfig()
    ): SortResult<T> = items.let {
        SortResult.flat(if (config.reverse) it.asReversed() else it)
    }
}