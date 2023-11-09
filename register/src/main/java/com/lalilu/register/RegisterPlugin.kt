package com.lalilu.register

import com.android.build.api.instrumentation.InstrumentationScope
import com.android.build.api.variant.AndroidComponentsExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class RegisterPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val androidComponents = project.extensions.getByType(AndroidComponentsExtension::class.java)
        println("RegisterPlugin: ${androidComponents.pluginVersion}")

        val map = hashMapOf<String, CollectSettings>()
        androidComponents.onVariants { variant ->
            variant.instrumentation.transformClassesWith(
                ScanClassVisitorFactory::class.java,
                InstrumentationScope.PROJECT
            ) {
                it.registerMap.set(map)
            }
            variant.instrumentation.transformClassesWith(
                InjectClassVisitorFactory::class.java,
                InstrumentationScope.PROJECT
            ) {
                it.registerMap.set(map)
            }
        }
    }
}