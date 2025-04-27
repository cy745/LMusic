import com.android.build.api.dsl.CommonExtension
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeFeatureFlag
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.application) apply false
    alias(libs.plugins.library) apply false
    alias(libs.plugins.kotlin) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.krouter.plugin)
    alias(libs.plugins.lumo)
}

// 配置注入遍历的起点项目
ext { set("targetInjectProjectName", "app") }

allprojects {
    tasks.withType<KotlinCompile> {
        compilerOptions { jvmTarget = JvmTarget.JVM_21 }
    }

    afterEvaluate {
        if (project.plugins.hasPlugin("com.android.library") ||
            project.plugins.hasPlugin("com.android.application")
        ) {
            project.extensions.configure(CommonExtension::class) {
                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_21
                    targetCompatibility = JavaVersion.VERSION_21
                }
            }
        }

        // 全局统一控制Compose相关参数配置
        if (project.plugins.hasPlugin("org.jetbrains.kotlin.plugin.compose")) {
            project.extensions.configure<ComposeCompilerGradlePluginExtension> {
                featureFlags.add(ComposeFeatureFlag.PausableComposition)
                featureFlags.add(ComposeFeatureFlag.StrongSkipping)
                featureFlags.add(ComposeFeatureFlag.OptimizeNonSkippingGroups)
                featureFlags.add(ComposeFeatureFlag.IntrinsicRemember)
            }
        }
    }
}
