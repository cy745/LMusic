package com.lalilu.extension_core.loader

import android.content.Context
import com.lalilu.extension_core.Constants
import com.lalilu.extension_core.ExtensionEnvironment
import com.lalilu.extension_core.ExtensionLoadResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred

/**
 * 获取宿主App中定义的插件
 */
class HostExtensionLoader : ExtensionLoader {
    override suspend fun loadExtension(
        context: Context,
        scope: CoroutineScope
    ): List<Deferred<ExtensionLoadResult>> {
        val packageManager = context.packageManager
        val packageInfo =
            packageManager.getPackageInfo(context.packageName, Constants.PACKAGE_FLAGS)

        return runCatching {
            val classes = getExtensionListByReflection(context.classLoader)
            val environment = ExtensionEnvironment.Package(packageInfo)

            loadExtensionWithClassLoader(scope, classes, context.classLoader, environment)
        }.getOrElse {
            println("""[loadHostExtensions] Error: ${it.message}""")
            it.printStackTrace()
            emptyList()
        }
    }
}