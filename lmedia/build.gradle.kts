plugins {
    alias(libs.plugins.library)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
}

android {
    compileSdk = libs.versions.compile.version.get().toIntOrNull()
    namespace = "com.lalilu.lmedia"

    buildFeatures {
        compose = true
    }

    defaultConfig {
        minSdk = libs.versions.min.sdk.version.get().toIntOrNull()
    }
    buildTypes {
        release {
            consumerProguardFiles("proguard-rules.pro")
        }
    }
    lint {
        disable += "FlowOperatorInvokedInComposition"
        disable += "CoroutineCreationDuringComposition"
    }
    externalNativeBuild {
        cmake {
            path = File("src/main/cpp/CMakeLists.txt")
        }
    }
}

dependencies {
    implementation(project(":common"))
    implementation(libs.startup.runtime)
    ksp(libs.koin.compiler)

    implementation(libs.kotlin.serialization)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.xmlutil.core)
    implementation(libs.xmlutil.serialization)

    api("androidx.media3:media3-common:1.5.1")

    // https://github.com/sachiotomita/kanhira
    // https://github.com/cy745/kanhira
    // 汉字转平假名库
    implementation("com.github.cy745:kanhira:2de73b1f0a")
}