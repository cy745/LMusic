package com.lalilu.common.base

sealed interface SourceType {
    data object Unknown : SourceType
    data object MediaStore : SourceType
    data object Local : SourceType
    data object Network : SourceType

    data class Extension(val extensionName: String) : SourceType
}