package com.lalilu.lmusic.screen.bean

const val SORT_BY_TIME = 0
const val SORT_BY_TEXT = 1

fun <T> sort(
    sortBy: Int,
    sortDesc: Boolean,
    list: MutableList<T>,
    getTimeField: (T) -> Long,
    getTextField: (T) -> String
): List<T> {
    when (sortBy) {
        SORT_BY_TIME -> if (sortDesc) {
            list.sortByDescending(getTimeField)
        } else {
            list.sortBy(getTimeField)
        }
        SORT_BY_TEXT -> if (sortDesc) {
            list.sortByDescending(getTextField)
        } else {
            list.sortBy(getTextField)
        }
    }
    return list
}

fun next(sortBy: Int): Int {
    return (sortBy + 1) % 2
}
