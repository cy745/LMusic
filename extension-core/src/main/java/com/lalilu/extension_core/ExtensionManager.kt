package com.lalilu.extension_core

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import dalvik.system.PathClassLoader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

object ExtensionManager : CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.Default

    private val PACKAGE_FLAGS = PackageManager.GET_CONFIGURATIONS or
            PackageManager.GET_META_DATA or
            PackageManager.GET_SIGNATURES or
            (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) PackageManager.GET_SIGNING_CERTIFICATES else 0)

    private var loadingJob: Job? = null
    val isLoadingFlow = MutableStateFlow(false)
    val extensionsFlow = MutableStateFlow<List<ExtensionLoadResult>>(emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    fun requireExtensionByPackageName(packageName: String): Flow<ExtensionLoadResult?> {
        return extensionsFlow.mapLatest { list ->
            list.firstOrNull { it.packageName == packageName }
        }
    }

    fun loadExtensions(context: Context) {
        loadingJob?.cancel()
        loadingJob = launch {
            isLoadingFlow.emit(true)

            val packageManager = context.packageManager
            val installedPackages = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getInstalledPackages(PackageManager.PackageInfoFlags.of(PACKAGE_FLAGS.toLong()))
            } else {
                packageManager.getInstalledPackages(PACKAGE_FLAGS)
            }

            if (!isActive) return@launch
            val results = installedPackages
                .asSequence()
                .filter { it.reqFeatures.orEmpty().any { it.name == "lmusic.extension" } }
                .map { loadExtension(this, context, it) }
                .toList()
                .awaitAll()

            if (!isActive) return@launch
            extensionsFlow.emit(results)
            isLoadingFlow.emit(false)
        }
    }

    private fun loadExtension(
        scope: CoroutineScope,
        context: Context,
        packageInfo: PackageInfo
    ): Deferred<ExtensionLoadResult> = scope.async {
        var errorMessage = ""
        val extension = runCatching {
            val appInfo = packageInfo.applicationInfo
            val classLoader = PathClassLoader(appInfo.sourceDir, null, context.classLoader)
            val clazz = Class.forName("com.lalilu.extension.Main", false, classLoader)

            clazz.getDeclaredConstructor().newInstance() as? Extension
        }.getOrElse {
            errorMessage = it.message ?: "Unknown error"
            it.printStackTrace()
            null
        }

        if (extension != null) {
            return@async ExtensionLoadResult.Ready(
                version = packageInfo.versionName,
                baseVersion = packageInfo.versionName,
                packageName = packageInfo.packageName,
                extension = extension
            )
        }

        ExtensionLoadResult.Error(
            version = packageInfo.versionName,
            baseVersion = packageInfo.versionName,
            packageName = packageInfo.packageName,
            message = errorMessage
        )
    }
}