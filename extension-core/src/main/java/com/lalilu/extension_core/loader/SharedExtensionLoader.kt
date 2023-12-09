package com.lalilu.extension_core.loader

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import com.lalilu.extension_core.Constants
import com.lalilu.extension_core.ExtensionClassLoader
import com.lalilu.extension_core.ExtensionEnvironment
import com.lalilu.extension_core.ExtensionLoadResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred

/**
 * 加载本机中已安装的其他插件，实际通过包管理器获取
 */
class SharedExtensionLoader : ExtensionLoader {
    override suspend fun loadExtension(
        context: Context,
        scope: CoroutineScope
    ): List<Deferred<ExtensionLoadResult>> {
        val packageManager = context.packageManager
        val installedPackages = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getInstalledPackages(PackageManager.PackageInfoFlags.of(Constants.PACKAGE_FLAGS.toLong()))
        } else {
            packageManager.getInstalledPackages(Constants.PACKAGE_FLAGS)
        }

        val sharedPackageInfo = installedPackages
            .asSequence()
            .filter {
                it.reqFeatures.orEmpty().any { it.name == Constants.EXTENSION_FEATURE_NAME }
            }.toList()

        return sharedPackageInfo.map { packageInfo ->
            runCatching {
                val classLoader = ExtensionClassLoader(
                    dexPath = packageInfo.applicationInfo.sourceDir,
                    parent = context.classLoader
                )
                val classes = getExtensionListFromMeta(packageInfo).toMutableSet()
                classes += getExtensionListByReflection(classLoader)
                val environment = ExtensionEnvironment.Package(packageInfo)

                loadExtensionWithClassLoader(scope, classes.toList(), classLoader, environment)
            }.getOrElse {
                println("""[loadSharedExtensions] Error: ${it.message}""")
                it.printStackTrace()
                emptyList()
            }
        }.flatten()
    }

    private fun getExtensionListFromMeta(
        packageInfo: PackageInfo,
    ): List<String> {
        val packageName = packageInfo.packageName
        val appInfo = packageInfo.applicationInfo

        return appInfo.metaData
            .getString(Constants.EXTENSION_META_DATA_CLASS)
            ?.trim()
            ?.takeIf(String::isNotBlank)
            ?.split(";")
            ?.map { if (it.startsWith(".")) packageName + it else it }
            ?: emptyList()
    }
}