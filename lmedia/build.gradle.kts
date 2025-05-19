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

    api(libs.media3.common)

    // https://github.com/sachiotomita/kanhira
    // https://github.com/cy745/kanhira
    // 汉字转平假名库
    implementation("com.github.cy745:kanhira:2de73b1f0a")
}