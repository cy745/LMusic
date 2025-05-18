package com.lalilu.lmedia.extension.sortable

import androidx.compose.runtime.Stable
import java.io.Serializable


sealed class GroupId : Serializable {
    data object None : GroupId() {
        private fun readResolve(): Any = None
    }

    data class FirstLetter(val letter: String) : GroupId()
    data class DiskNumber(val number: Int) : GroupId()
    data class Time(val time: String) : GroupId()

    val text: String by lazy {
        when (this) {
            is DiskNumber -> number.toString()
            is FirstLetter -> letter
            is Time -> time
            None -> "NONE"
        }
    }

    override fun toString(): String = text
}

interface ItemExtraData {
    data class TrackNumber(val number: Int) : ItemExtraData
    data class PlayedCount(val count: Int) : ItemExtraData
}

data class SortedGroup<T : Sortable>(
    val groupId: GroupId?,
    val items: List<T>,
    val extras: List<ItemExtraData?> = emptyList()
)

data class SortResult<T : Sortable>(
    val groups: List<SortedGroup<T>>,
) {
    val itemList: List<T> by lazy { groups.flatMap { it.items } }

    inline fun draw(onGroup: SortedGroup<T>.() -> Unit) {
        groups.forEach { group -> group.onGroup() }
    }

    companion object {
        @Stable
        fun <T : Sortable> flat(items: List<T>): SortResult<T> =
            SortResult(listOf(SortedGroup(null, items)))

        @Stable
        inline fun <reified T : Sortable> empty(): SortResult<T> = SortResult(emptyList())
    }
}