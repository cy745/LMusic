plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    namespace = "com.lalilu.component"
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
    api(libs.lottie.compose)

    api(libs.bundles.voyager)

    // accompanist
    // https://google.github.io/accompanist
    api(libs.bundles.accompanist)

    api(project(":lmedia"))
    api(project(":common"))

    api(libs.coil)
    api(libs.coil.compose)

    // https://github.com/Calvin-LL/Reorderable
    // Apache-2.0 license
    api("sh.calvin.reorderable:reorderable:1.1.0")

    implementation(libs.activity.compose)
    // compose
    api(libs.compose.compiler)
    api(platform(libs.compose.bom))
    api(libs.bundles.compose)
    debugApi(libs.bundles.compose.debug)
}