// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.application) apply false
    alias(libs.plugins.library) apply false
    alias(libs.plugins.kotlin) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.flyjingfish.aop) apply false
}

gradle.taskGraph.whenReady {
    allTasks.onEach {
        // 避免ksp类型任务被跳过
        if (it.name.startsWith("ksp") && it.name.endsWith("Kotlin")) {
            it.setOnlyIf { true }
            it.outputs.upToDateWhen { false }
        }
    }
}