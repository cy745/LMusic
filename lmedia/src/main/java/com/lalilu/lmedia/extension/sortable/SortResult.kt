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

sealed interface SortResult<T : Sortable> {
    val itemList: List<T>

    data class Grouped<T : Sortable>(val groups: List<SortedGroup<T>>) : SortResult<T> {
        override val itemList: List<T> by lazy { groups.flatMap { it.items } }
    }

    data class Flat<T : Sortable>(val items: List<T>) : SortResult<T> {
        override val itemList: List<T> = items
    }

    companion object {
        @Stable
        inline fun <reified T : Sortable> empty(): SortResult<T> = Flat(emptyList())
    }
}