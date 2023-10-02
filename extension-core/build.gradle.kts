plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    namespace = "com.lalilu.extension_core"
    compileSdk = 34

    buildFeatures {
        compose = true
    }

    defaultConfig {
        minSdk = 21
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    composeOptions {
        kotlinCompilerExtensionVersion = findProperty("compose_compiler_version").toString()
    }
}

dependencies {
    api(project(":extension-ksp"))

    // https://github.com/coil-kt/coil
    // Apache-2.0 License
    // 图片加载库
    api("io.coil-kt:coil:${findProperty("coil_version")}")
    api("io.coil-kt:coil-compose:${findProperty("coil_version")}")

    // compose
    api("androidx.compose.compiler:compiler:${findProperty("compose_compiler_version")}")
    api(platform("androidx.compose:compose-bom:${findProperty("compose_bom_version")}"))
    api("androidx.compose.ui:ui")
    api("androidx.compose.ui:ui-util")
    api("androidx.compose.ui:ui-graphics")
    api("androidx.compose.ui:ui-viewbinding")
    api("androidx.compose.foundation:foundation")
    api("androidx.compose.material3:material3")
    api("androidx.compose.material3:material3-window-size-class")
    api("androidx.compose.runtime:runtime-livedata")
    api("androidx.compose.material:material")
    debugApi("androidx.compose.ui:ui-tooling")
    debugApi("androidx.compose.ui:ui-tooling-preview")
}