package com.lalilu.extension_core

import android.content.Context
import android.content.pm.PackageInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

sealed class ExtensionLoadResult(
    val className: String,
    val packageInfo: PackageInfo
) {
    class OutOfDated(
        className: String,
        packageInfo: PackageInfo
    ) : ExtensionLoadResult(className, packageInfo)

    class Error(
        className: String,
        packageInfo: PackageInfo,
        val message: String
    ) : ExtensionLoadResult(className, packageInfo)

    class Ready(
        className: String,
        packageInfo: PackageInfo,
        val extension: Extension
    ) : ExtensionLoadResult(className, packageInfo)

    @Composable
    fun Place(
        context: Context = LocalContext.current,
        contentKey: String,
        errorPlaceHolder: @Composable () -> Unit = {},
    ) {
        if (this !is Ready) {
            errorPlaceHolder()
            return
        }

        val tempContext = remember(context) {
            runCatching { context.createPackageContext(packageInfo.packageName, 0) }.getOrNull()
        }
        val content = remember(contentKey) {
            extension.getContentMap()[contentKey]?.takeIf { it !== EMPTY_CONTENT }
        }

        if (tempContext != null && content != null) {
            CompositionLocalProvider(LocalContext provides tempContext, content = content)
        } else {
            errorPlaceHolder()
        }
    }
}
