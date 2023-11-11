package com.lalilu.register

import com.android.build.api.artifact.ScopedArtifact
import com.android.build.api.instrumentation.InstrumentationScope
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.ScopedArtifacts
import com.android.build.gradle.AppPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

class RegisterPlugin : Plugin<Project> {
    companion object {
        const val extensionName: String = "registerPlugin"
    }

    override fun apply(project: Project) {
        project.extensions.create(extensionName, RegisterConfig::class.java)

        val isApp = project.plugins.hasPlugin(AppPlugin::class.java)
        require(isApp) { "RegisterPlugin should be apply to App project." }

        val config = project.extensions.findByName(extensionName) as? RegisterConfig
        requireNotNull(config) { "RegisterConfig hasn't been initialized." }

        val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)
        println("RegisterPlugin: ${androidComponents.pluginVersion}")

        var registerInfo: List<RegisterInfo>? = null
        androidComponents.onVariants { variant ->
            if (registerInfo == null) {
                registerInfo = config.convertRegisterInfo()
                RegisterConfig.registerInfo.putAll(registerInfo!!.associateBy { it.targetManagerClass })
            }

            if (registerInfo.isNullOrEmpty()) {
                println("RegisterPlugin: No register info found.")
                return@onVariants
            }

            // 扫描所有需要注册的Item
            variant.instrumentation.transformClassesWith(
                ScanClassVisitorFactory::class.java,
                InstrumentationScope.ALL
            ) {
                it.temp.set(System.currentTimeMillis())
            }

            val injectTransformTask = project.tasks
                .register("InjectTransformTask_${variant.name}", InjectTransformTask::class.java) {
                    intermediate.set(project.layout.buildDirectory.dir("intermediates/inject_result/${variant.name}"))
                }

            variant.artifacts
                .forScope(ScopedArtifacts.Scope.ALL)
                .use(injectTransformTask)
                .toTransform(
                    ScopedArtifact.CLASSES,
                    InjectTransformTask::allJars,
                    InjectTransformTask::allDirectories,
                    InjectTransformTask::outputJar
                )
        }
    }
}