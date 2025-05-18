package com.lalilu.lmedia.extension.sortable

import kotlinx.serialization.Serializable

@Serializable
data class SortConfig(
    val hideGroup: Boolean = false,
    val reverse: Boolean = false,
    val hideItemExtra: Boolean = false,
)