package com.lalilu.extension_core

sealed class ExtensionLoadResult(
    val version: String,
    val baseVersion: String,
    val packageName: String,
    val extension: Extension? = null
) {
    class OutOfDate(
        version: String,
        baseVersion: String,
        packageName: String
    ) : ExtensionLoadResult(version, baseVersion, packageName)

    class Error(
        version: String,
        baseVersion: String,
        packageName: String,
        val message: String
    ) : ExtensionLoadResult(version, baseVersion, packageName)


    class Ready(
        version: String,
        baseVersion: String,
        packageName: String,
        extension: Extension
    ) : ExtensionLoadResult(version, baseVersion, packageName, extension)
}
