plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    namespace = "com.lalilu.lplayer"
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
    implementation(project(":common"))
    implementation(project(":lmedia"))
    implementation(libs.startup.runtime)
    implementation(libs.coil3.android)

    api(project(":lplayer:lib-decoder-flac"))
    api(libs.bundles.media3)
    api("com.github.cy745:fpcalc:75daa4514f")
}