package com.lalilu.extension_core

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import dalvik.system.PathClassLoader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

object ExtensionManager : CoroutineScope, LifecycleEventObserver {
    private const val EXTENSION_FEATURE_NAME = "lmusic.extension"
    private const val EXTENSION_META_DATA_CLASS = "lmusic.extension.class"

    override val coroutineContext: CoroutineContext = Dispatchers.Default

    private val PACKAGE_FLAGS = PackageManager.GET_CONFIGURATIONS or
            PackageManager.GET_META_DATA or
            PackageManager.GET_SIGNATURES or
            (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) PackageManager.GET_SIGNING_CERTIFICATES else 0)

    private var debounceJob: Job? = null
    private var loadingJob: Job? = null
    val isLoadingFlow = MutableStateFlow(false)
    val extensionsFlow = MutableStateFlow<List<ExtensionLoadResult>>(emptyList())

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context, p1: Intent) {
            debounceJob?.cancel()
            debounceJob = launch {
                delay(500)
                if (!isActive) return@launch
                loadExtensions(p0)
            }
        }
    }

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
                .filter { it.reqFeatures.orEmpty().any { it.name == EXTENSION_FEATURE_NAME } }
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
        var errorMessage = "Unknown error"
        val appInfo = packageInfo.applicationInfo
        val versionName = packageInfo.versionName
        val packageName = packageInfo.packageName

        val extension = runCatching {
            val className = appInfo.metaData
                .getString(EXTENSION_META_DATA_CLASS)
                ?.trim()
                ?.takeIf(String::isNotBlank)
                ?.let { if (it.startsWith(".")) packageName + it else it }
                ?: throw IllegalStateException("Extension $packageName entry class not found.")

            val classLoader = PathClassLoader(appInfo.sourceDir, null, context.classLoader)
            val clazz = Class.forName(className, false, classLoader)

            clazz.getDeclaredConstructor().newInstance() as? Extension
        }.getOrElse {
            it.printStackTrace()
            errorMessage = it.message ?: it.localizedMessage ?: "Unknown error"
            null
        }

        if (extension != null) {
            return@async ExtensionLoadResult.Ready(
                version = versionName,
                baseVersion = versionName,
                packageName = packageName,
                extension = extension
            )
        }

        ExtensionLoadResult.Error(
            version = versionName,
            baseVersion = versionName,
            packageName = packageName,
            message = errorMessage
        )
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        val activity = source as? Activity ?: return
        when (event) {
            Lifecycle.Event.ON_START -> {
                val intentFilter = IntentFilter().apply {
                    addAction(Intent.ACTION_PACKAGE_ADDED)
                    addAction(Intent.ACTION_PACKAGE_REMOVED)
                    addAction(Intent.ACTION_PACKAGE_REPLACED)
                    addAction(Intent.ACTION_PACKAGE_CHANGED)
                    addAction(Intent.ACTION_PACKAGE_DATA_CLEARED)
                    addDataScheme("package")
                }
                activity.registerReceiver(broadcastReceiver, intentFilter)
            }

            Lifecycle.Event.ON_DESTROY -> {
                activity.unregisterReceiver(broadcastReceiver)
            }

            else -> Unit
        }
    }
}