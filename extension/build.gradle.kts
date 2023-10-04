import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    kotlin("android")
    id("com.google.devtools.ksp")
}

val keystoreProps = rootProject.file("keystore.properties")
    .takeIf { it.exists() }
    ?.let { Properties().apply { load(FileInputStream(it)) } }

android {
    namespace = "com.lalilu.extension"
    compileSdk = AndroidConfig.COMPILE_SDK_VERSION

    buildFeatures {
        compose = true
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.lalilu.extension"
        minSdk = AndroidConfig.MIN_SDK_VERSION
        targetSdk = AndroidConfig.TARGET_SDK_VERSION
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

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.version.get()
    }
}

configurations {
    compileOnly {
        isCanBeResolved = true
    }
}

dependencies {
    compileOnly(libs.kotlin.stdlib)
    compileOnly(project(":extension-core"))
    ksp(project(":extension-ksp"))
}