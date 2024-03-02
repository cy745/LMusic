package com.lalilu.extension_core.loader

import android.content.Context
import com.lalilu.extension_core.Constants
import com.lalilu.extension_core.Extension
import com.lalilu.extension_core.ExtensionEnvironment
import com.lalilu.extension_core.ExtensionLoadResult
import com.lalilu.extension_core.ExtensionMetadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async

interface ExtensionLoader {

    suspend fun loadExtension(
        context: Context,
        scope: CoroutineScope
    ): List<Deferred<ExtensionLoadResult>>

    fun getExtensionListByReflection(
        classLoader: ClassLoader,
    ): List<String> {
        return runCatching {
            val targetClass = Constants.EXTENSION_SOURCES_CLASS
            val clazz = Class.forName(targetClass, false, classLoader)
            val method = clazz.getDeclaredMethod("getClasses").apply { isAccessible = true }
            val obj = clazz.getDeclaredConstructor().newInstance()

            (method.invoke(obj) as List<*>).mapNotNull { it as? String }
        }.getOrElse {
            it.printStackTrace()
            emptyList()
        }
    }

    fun loadExtensionWithClassLoader(
        scope: CoroutineScope,
        classes: List<String>,
        classLoader: ClassLoader,
        environment: ExtensionEnvironment,
    ): List<Deferred<ExtensionLoadResult>> {
        return classes.map { className ->
            scope.async {
                var errorMessage = "Unknown error"

                // 加载Extension对象
                val extension = runCatching {
                    val clazz = Class.forName(className, false, classLoader)

                    clazz.getDeclaredConstructor().newInstance() as? Extension
                }.getOrElse {
                    println("""[loadExtensionWithClassLoader] Error: ${it.message}""")
                    it.printStackTrace()
                    errorMessage = it.message ?: it.localizedMessage ?: "Unknown error"
                    null
                }
                // TODO 待完善metadata的获取逻辑
                val extMetadata = ExtensionMetadata(
                    extId = className,
                    name = "",
                    intro = "",
                    versionName = "",
                    versionNumber = 0,
                )

                if (extension != null) {
                    return@async ExtensionLoadResult.Ready(
                        extId = className,
                        metadata = extMetadata,
                        classLoader = classLoader,
                        extension = extension,
                        environment = environment
                    )
                }

                ExtensionLoadResult.Error(
                    extId = className,
                    metadata = extMetadata,
                    message = errorMessage
                )
            }
        }
    }
}

