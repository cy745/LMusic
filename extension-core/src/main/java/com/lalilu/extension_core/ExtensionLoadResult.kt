package com.lalilu.extension_core

import android.content.Context
import android.content.pm.PackageInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

sealed class ExtensionLoadResult(
    val packageInfo: PackageInfo
) {
    class OutOfDated(packageInfo: PackageInfo) : ExtensionLoadResult(packageInfo)
    class Error(packageInfo: PackageInfo, val message: String) : ExtensionLoadResult(packageInfo)

    class Ready(
        packageInfo: PackageInfo,
        val extension: Extension
    ) : ExtensionLoadResult(packageInfo) {

        @Composable
        fun Place(
            context: Context = LocalContext.current,
            errorPlaceHolder: @Composable () -> Unit = {},
            content: @Composable () -> Unit,
        ) {
            val tempContext = remember {
                runCatching { context.createPackageContext(packageInfo.packageName, 0) }.getOrNull()
            }

            if (tempContext != null) {
                CompositionLocalProvider(LocalContext provides tempContext, content = content)
            } else {
                errorPlaceHolder()
            }
        }
    }
}
