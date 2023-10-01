package com.lalilu.extension_core

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

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
    ) : ExtensionLoadResult(version, baseVersion, packageName, extension) {

        @Composable
        fun Place(
            context: Context = LocalContext.current,
            errorPlaceHolder: @Composable () -> Unit = {},
            content: @Composable () -> Unit,
        ) {
            val tempContext = remember {
                runCatching { context.createPackageContext(packageName, 0) }.getOrNull()
            }

            if (tempContext != null) {
                CompositionLocalProvider(LocalContext provides tempContext, content = content)
            } else {
                errorPlaceHolder()
            }
        }
    }
}
