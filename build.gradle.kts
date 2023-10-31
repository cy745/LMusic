// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.application) apply false
    alias(libs.plugins.library) apply false
    alias(libs.plugins.kotlin) apply false
    alias(libs.plugins.ksp) apply false
}

buildscript {
    dependencies {
        classpath(libs.kotlin.serialization)
    }
}

gradle.taskGraph.whenReady {
    allTasks.onEach {
        // 避免ksp类型任务被跳过
        if (it.name == "kspDebugKotlin") {
            it.setOnlyIf { true }
            it.outputs.upToDateWhen { false }
        }
    }
}