package com.lalilu.extension_core

import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageInfo
import android.content.res.Resources
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext

data class ExtensionMetadata(
    val extId: String,
    val name: String,
    val intro: String,
    val versionName: String,
    val versionNumber: Int,
)

sealed interface ExtensionEnvironment {
    data class Package(val packageInfo: PackageInfo) : ExtensionEnvironment
    data class Apk(val resources: Resources) : ExtensionEnvironment
}

sealed class ExtensionLoadResult(
    open val extId: String,
    open val metadata: ExtensionMetadata
) {
    data class Error(
        override val extId: String,
        override val metadata: ExtensionMetadata,
        val message: String
    ) : ExtensionLoadResult(extId, metadata)

    data class Ready(
        override val extId: String,
        override val metadata: ExtensionMetadata,
        val extension: Extension,
        val classLoader: ClassLoader,
        val environment: ExtensionEnvironment,
        val isOutOfDated: Boolean = false
    ) : ExtensionLoadResult(extId, metadata)
}


@Composable
fun ExtensionLoadResult.Place(
    context: Context = LocalContext.current,
    contentKey: String,
    params: Map<String, String> = emptyMap(),
    errorPlaceHolder: @Composable () -> Unit = {},
) {
    if (this !is ExtensionLoadResult.Ready) {
        errorPlaceHolder()
        return
    }

    val configuration = LocalConfiguration.current
    val tempContext = remember(context) {
        runCatching {
            this.environment.let { environment ->
                when (environment) {
                    is ExtensionEnvironment.Apk -> {
                        object : ContextWrapper(context.createConfigurationContext(configuration)) {
                            override fun getResources(): Resources = environment.resources
                        }
                    }

                    is ExtensionEnvironment.Package -> {
                        context.createPackageContext(environment.packageInfo.packageName, 0)
                    }
                }
            }
        }.getOrNull()
    }
    val content = remember(contentKey) {
        extension.getContentMap()[contentKey]?.takeIf { it !== EMPTY_CONTENT }
    }

    if (tempContext != null && content != null) {
        CompositionLocalProvider(LocalContext provides tempContext) { content(params) }
    } else {
        errorPlaceHolder()
    }
}