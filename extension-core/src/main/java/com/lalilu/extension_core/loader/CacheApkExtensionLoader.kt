package com.lalilu.extension_core.loader

import android.content.Context
import android.content.res.AssetManager
import android.content.res.Resources
import com.lalilu.extension_core.ExtensionClassLoader
import com.lalilu.extension_core.ExtensionEnvironment
import com.lalilu.extension_core.ExtensionLoadResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import java.io.File

/**
 * 加载在宿主应用的cache目录下的apk插件
 */
class CacheApkExtensionLoader : ExtensionLoader {
    override suspend fun loadExtension(
        context: Context,
        scope: CoroutineScope
    ): List<Deferred<ExtensionLoadResult>> {
        val cacheDictionary = File(context.cacheDir, "ext_apk")
        if (!cacheDictionary.exists()) cacheDictionary.mkdir()
        if (!cacheDictionary.isDirectory) return emptyList()

        val childList = cacheDictionary.listFiles() ?: return emptyList()

        return childList.filter { it.extension.uppercase() == "APK" }
            .map { file ->
                // 创建该Extension专用的ClassLoader
                val classLoader = ExtensionClassLoader(file.absolutePath, context.classLoader)
                val classes = getExtensionListByReflection(classLoader)

                // 读取该Apk内的resources
                val resources = createResource(context, file.absolutePath)
                val environment = ExtensionEnvironment.Apk(resources)

                loadExtensionWithClassLoader(
                    scope,
                    classes,
                    classLoader,
                    environment
                )
            }.flatten()
    }

    @Suppress("DEPRECATION")
    private fun createResource(context: Context, path: String): Resources {
        val assetManagerClass = AssetManager::class.java
        val assetManager = assetManagerClass.getDeclaredConstructor().newInstance()
        val method = assetManagerClass.getMethod("addAssetPath", String::class.java)

        method.isAccessible = true
        method.invoke(assetManager, path)

        return Resources(
            assetManager,
            context.resources.displayMetrics,
            context.resources.configuration
        )
    }
}