plugins {
    id("com.android.library")
    id("kotlinx-serialization")
    kotlin("android")
}

android {
    namespace = "com.lalilu.common"
    compileSdk = AndroidConfig.COMPILE_SDK_VERSION

    defaultConfig {
        minSdk = AndroidConfig.MIN_SDK_VERSION
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
    api(libs.kotlinx.serialization.json)

    api(libs.utilcodex)
    api(libs.appcompat)
    api(libs.core.ktx)
    api(libs.palette.ktx)
    api(libs.dynamicanimation.ktx)
    api(libs.media)

    api(libs.koin.android)
    api(libs.koin.compose)
}