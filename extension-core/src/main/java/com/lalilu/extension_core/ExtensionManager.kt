package com.lalilu.extension_core

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.lalilu.extension_core.loader.CacheApkExtensionLoader
import com.lalilu.extension_core.loader.HostExtensionLoader
import com.lalilu.extension_core.loader.SharedExtensionLoader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

@OptIn(ExperimentalCoroutinesApi::class)
object ExtensionManager : CoroutineScope, LifecycleEventObserver {
    override val coroutineContext: CoroutineContext = Dispatchers.Default

    private var debounceJob: Job? = null
    private var loadingJob: Job? = null
    private val isLoadingFlow = MutableStateFlow(false)
    val extensionsFlow = MutableStateFlow<List<ExtensionLoadResult>>(emptyList())
    private val loaders = listOf(
        CacheApkExtensionLoader(),
        HostExtensionLoader(),
        SharedExtensionLoader()
    )

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

    fun loadExtensions(context: Context) {
        loadingJob?.cancel()
        loadingJob = launch {
            isLoadingFlow.emit(true)

            val result = loaders
                .map { it.loadExtension(context, this) }
                .flatten()
                .awaitAll()

            if (!isActive) return@launch
            extensionsFlow.emit(result)
            isLoadingFlow.emit(false)
        }
    }

    fun requireExtensionByPackageName(packageName: String): Flow<ExtensionLoadResult?> {
        return extensionsFlow.mapLatest { list ->
            list.firstOrNull {
                it is ExtensionLoadResult.Ready &&
                        it.environment is ExtensionEnvironment.Package &&
                        it.environment.packageInfo.packageName == packageName
            }
        }
    }

    fun requireExtensionByClassName(className: String): Flow<ExtensionLoadResult?> {
        return extensionsFlow.mapLatest { list -> list.firstOrNull { it.extId == className } }
    }

    fun requireExtensionByContentKey(contentKey: String): Flow<List<ExtensionLoadResult.Ready>> {
        return extensionsFlow.mapLatest { list ->
            list.mapNotNull { result ->
                (result as? ExtensionLoadResult.Ready)
                    ?.takeIf {
                        val content = it.extension.getContentMap()[contentKey]
                        content != null && content !== EMPTY_CONTENT
                    }
            }
        }
    }

    fun requireProviderFromExtensions(): List<Provider> {
        return extensionsFlow.value
            .filterIsInstance<ExtensionLoadResult.Ready>()
            .mapNotNull { runCatching { it.extension.getProvider() }.getOrNull() }
    }

    fun requireProviderFlowFromExtensions(): Flow<List<Provider>> {
        return extensionsFlow.mapLatest { list ->
            list.filterIsInstance<ExtensionLoadResult.Ready>()
                .mapNotNull { runCatching { it.extension.getProvider() }.getOrNull() }
        }
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