plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    namespace = "com.lalilu.extension_core"
    compileSdk = AndroidConfig.COMPILE_SDK_VERSION

    buildFeatures {
        compose = true
    }

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
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.version.get()
    }
}

dependencies {
    api(project(":lmedia"))
    api(project(":common"))

    api(libs.coil)
    api(libs.coil.compose)

    // compose
    api(libs.compose.compiler)
    api(platform(libs.compose.bom))
    api(libs.bundles.compose)
    debugApi(libs.bundles.compose.debug)
}