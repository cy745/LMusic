package com.lalilu.lmusic.compose.new_screen

import android.content.Context
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import com.lalilu.extension_core.ExtensionLoadResult
import com.lalilu.extension_core.ExtensionManager
import com.ramcosta.composedestinations.annotation.Destination


@Destination
@Composable
fun ExtensionHostScreen(
    packageName: String,
    context: Context = LocalContext.current
) {
    val extension by ExtensionManager
        .requireExtensionByPackageName(packageName)
        .collectAsState(null)

    if (extension is ExtensionLoadResult.Ready) {
        val tempContext = context.createPackageContext(extension!!.packageName, 0)
        CompositionLocalProvider(LocalContext provides tempContext) {
            extension!!.extension!!.mainContent()
        }
    } else {
        val message = when (extension) {
            is ExtensionLoadResult.Error -> (extension as ExtensionLoadResult.Error).message
            is ExtensionLoadResult.OutOfDate -> "Extension $packageName is out of dated."
            else -> "Extension $packageName is not found."
        }

        Text(text = message)
    }
}