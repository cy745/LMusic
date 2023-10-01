package com.lalilu.lmusic.compose.new_screen

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.lalilu.extension_core.ExtensionLoadResult
import com.lalilu.extension_core.ExtensionManager
import com.ramcosta.composedestinations.annotation.Destination


@Destination
@Composable
fun ExtensionHostScreen(
    packageName: String
) {
    val extensionResult by ExtensionManager
        .requireExtensionByPackageName(packageName)
        .collectAsState(null)

    extensionResult?.let { it as? ExtensionLoadResult.Ready }?.apply {
        Place(
            content = extension.mainContent,
            errorPlaceHolder = {
                Text(text = "LoadError ${packageInfo.packageName}")
            },
        )
    } ?: run {
        val message = when (extensionResult) {
            is ExtensionLoadResult.Error -> (extensionResult as ExtensionLoadResult.Error).message
            is ExtensionLoadResult.OutOfDated -> "Extension $packageName is out of dated."
            else -> "Extension $packageName is not found."
        }
        Text(text = message)
    }
}