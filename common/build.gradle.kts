plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    namespace = "com.lalilu.common"
    compileSdk = libs.versions.compile.version.get().toIntOrNull()

    defaultConfig {
        minSdk = libs.versions.min.sdk.version.get().toIntOrNull()
    }
    buildTypes {
        release {
            consumerProguardFiles("proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    api(libs.utilcodex)
    api(libs.appcompat)
    api(libs.core.ktx)
    api(libs.palette.ktx)
    api(libs.dynamicanimation.ktx)
    api(libs.media)

    api("com.russhwolf:multiplatform-settings:1.3.0")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-guava:1.9.0")

    api(libs.bundles.koin)
    api(libs.krouter.core)
}