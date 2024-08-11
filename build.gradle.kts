// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.application) apply false
    alias(libs.plugins.library) apply false
    alias(libs.plugins.kotlin) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.flyjingfish.aop) apply false
    alias(libs.plugins.compose.compiler) apply false
}

buildscript {
    dependencies { classpath(libs.krouter.plugin) }
}

ext { set("targetInjectProjectName", "app") }
apply(plugin = "krouter-plugin")
