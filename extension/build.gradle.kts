import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    kotlin("android")
}

val keystoreProps = rootProject.file("keystore.properties")
    .takeIf { it.exists() }
    ?.let { Properties().apply { load(FileInputStream(it)) } }

android {
    namespace = "com.lalilu.extension"
    compileSdk = 34

    buildFeatures {
        compose = true
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.lalilu.extension"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    if (keystoreProps != null) {
        val storeFileValue = keystoreProps["storeFile"]?.toString() ?: ""
        val storePasswordValue = keystoreProps["storePassword"]?.toString() ?: ""
        val keyAliasValue = keystoreProps["keyAlias"]?.toString() ?: ""
        val keyPasswordValue = keystoreProps["keyPassword"]?.toString() ?: ""

        if (storeFileValue.isNotBlank() && file(storeFileValue).exists()) {
            signingConfigs.create("release") {
                storeFile(file(storeFileValue))
                storePassword(storePasswordValue)
                keyAlias(keyAliasValue)
                keyPassword(keyPasswordValue)
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = kotlin.runCatching { signingConfigs["release"] }.getOrNull()
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
    composeOptions {
        kotlinCompilerExtensionVersion = findProperty("compose_compiler_version").toString()
    }
}

configurations {
    compileOnly {
        isCanBeResolved = true
    }
}

dependencies {
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib-jdk8:${findProperty("kotlin_version")}")
    compileOnly(project(":extension-core"))
}