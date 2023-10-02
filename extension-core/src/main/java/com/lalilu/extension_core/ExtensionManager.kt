package com.lalilu.extension_core

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.lalilu.extension_ksp.ExtProcessor
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

@OptIn(ExperimentalCoroutinesApi::class)
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

    fun requireExtensionByPackageName(packageName: String): Flow<ExtensionLoadResult?> {
        return extensionsFlow.mapLatest { list ->
            list.firstOrNull { it.packageInfo.packageName == packageName }
        }
    }

    fun requireExtensionByContent(contentFunc: (Extension) -> @Composable () -> Unit): Flow<List<ExtensionLoadResult.Ready>> {
        return extensionsFlow.mapLatest { list ->
            list.mapNotNull { result ->
                (result as? ExtensionLoadResult.Ready)
                    ?.takeIf { contentFunc(it.extension) !== EMPTY_CONTENT }
            }
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
            val hostExtensions = packageManager.getPackageInfo(context.packageName, PACKAGE_FLAGS)
                ?.let { loadHostExtensions(this, context, it) }
                ?: emptyList()

            if (!isActive) return@launch
            val sharedExtensions = installedPackages
                .asSequence()
                .filter { it.reqFeatures.orEmpty().any { it.name == EXTENSION_FEATURE_NAME } }
                .flatMap { loadSharedExtensions(this, context, it) }
                .toList()

            val result = sharedExtensions.awaitAll() + hostExtensions.awaitAll()
            if (!isActive) return@launch
            extensionsFlow.emit(result)
            isLoadingFlow.emit(false)
        }
    }

    private fun loadExtensionWithClassLoader(
        scope: CoroutineScope,
        classes: List<String>,
        packageInfo: PackageInfo,
        classLoader: ClassLoader
    ): List<Deferred<ExtensionLoadResult>> {
        return classes.map { className ->
            scope.async {
                var errorMessage = "Unknown error"
                val extension = runCatching {
                    val clazz = Class.forName(className, false, classLoader)

                    clazz.getDeclaredConstructor().newInstance() as? Extension
                }.getOrElse {
                    it.printStackTrace()
                    errorMessage = it.message ?: it.localizedMessage ?: "Unknown error"
                    null
                }

                if (extension != null) {
                    return@async ExtensionLoadResult.Ready(
                        className = className,
                        packageInfo = packageInfo,
                        extension = extension
                    )
                }

                ExtensionLoadResult.Error(
                    className = className,
                    packageInfo = packageInfo,
                    message = errorMessage
                )
            }
        }
    }

    private fun loadSharedExtensions(
        scope: CoroutineScope,
        context: Context,
        packageInfo: PackageInfo
    ): List<Deferred<ExtensionLoadResult>> {
        return runCatching {
            val classLoader = ForceClassLoader(
                forceClassName = arrayOf(ExtProcessor.GENERATE_CLASS_NAME),
                dexPath = packageInfo.applicationInfo.sourceDir,
                parent = context.classLoader
            )
            val classes = getExtensionListFromMeta(packageInfo).toMutableSet()
            classes += getExtensionListByReflection(classLoader)

            loadExtensionWithClassLoader(scope, classes.toList(), packageInfo, classLoader)
        }.getOrElse {
            it.printStackTrace()
            emptyList()
        }
    }

    private fun loadHostExtensions(
        scope: CoroutineScope,
        context: Context,
        packageInfo: PackageInfo
    ): List<Deferred<ExtensionLoadResult>> {
        return runCatching {
            val classes = getExtensionListByReflection(context.classLoader)

            loadExtensionWithClassLoader(scope, classes, packageInfo, context.classLoader)
        }.getOrElse {
            it.printStackTrace()
            emptyList()
        }
    }

    private fun getExtensionListFromMeta(
        packageInfo: PackageInfo
    ): List<String> {
        val packageName = packageInfo.packageName
        val appInfo = packageInfo.applicationInfo

        return appInfo.metaData
            .getString(EXTENSION_META_DATA_CLASS)
            ?.trim()
            ?.takeIf(String::isNotBlank)
            ?.split(";")
            ?.map { if (it.startsWith(".")) packageName + it else it }
            ?: emptyList()
    }

    private fun getExtensionListByReflection(
        classLoader: ClassLoader
    ): List<String> {
        return runCatching {
            val targetClass = ExtProcessor.GENERATE_CLASS_NAME
            val clazz = Class.forName(targetClass, false, classLoader)
            val method = clazz.getDeclaredMethod("getClasses").apply { isAccessible = true }
            val obj = clazz.getDeclaredConstructor().newInstance()

            (method.invoke(obj) as List<*>).mapNotNull { it as? String }
        }.getOrElse {
            it.printStackTrace()
            emptyList()
        }
    }

    private class ForceClassLoader(
        private vararg val forceClassName: String,
        dexPath: String,
        parent: ClassLoader
    ) : PathClassLoader(dexPath, null, parent) {
        private val cache = hashMapOf<String, Class<*>>()

        override fun loadClass(name: String, resolve: Boolean): Class<*> {
            return if (forceClassName.contains(name)) {
                cache[name]
                    ?: findClass(name)?.also { cache[name] = it }
                    ?: throw ClassNotFoundException("Class $name not found")
            } else {
                super.loadClass(name, resolve)
            }
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