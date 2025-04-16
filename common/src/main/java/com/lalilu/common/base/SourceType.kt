package com.lalilu.common.base

sealed class SourceType(val name: String) {
    data object Unknown : SourceType("Unknown")
    data object MediaStore : SourceType("MediaStore")
    data object Local : SourceType("Local")
    data object Network : SourceType("Network")
    data object WebDAV : SourceType("WebDAV")

    data class Extension(val extensionName: String) : SourceType(extensionName)
}