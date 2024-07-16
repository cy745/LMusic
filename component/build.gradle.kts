plugins {
    id("com.android.library")
    kotlin("android")
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.lalilu.component"
    compileSdk = libs.versions.compile.version.get().toIntOrNull()

    defaultConfig {
        minSdk = libs.versions.min.sdk.version.get().toIntOrNull()
    }

    buildFeatures {
        compose = true
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

composeCompiler {
    enableStrongSkippingMode = true
}

dependencies {
    api(libs.lottie.compose)

    api(libs.bundles.voyager)

    // accompanist
    // https://google.github.io/accompanist
    api(libs.bundles.accompanist)

    api(project(":lmedia"))
    api(project(":common"))
    api(project(":lplayer"))

    api(libs.bundles.coil3)

    // https://github.com/Calvin-LL/Reorderable
    // Apache-2.0 license
    api("sh.calvin.reorderable:reorderable:1.1.0")
    api("com.github.cy745:AnyPopDialog-Compose:jitpack-SNAPSHOT")
    api("me.rosuh:AndroidFilePicker:1.0.1")
    api("androidx.constraintlayout:constraintlayout-compose:1.1.0-alpha13")

    // compose
//    api(platform(libs.compose.bom))
    api(platform(libs.compose.bom.alpha))
    api(libs.activity.compose)
    api(libs.bundles.compose)
    api(libs.bundles.compose.debug)
}