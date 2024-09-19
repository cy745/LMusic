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
    implementation("com.github.cy745:AndroidVideoCache:2.7.2")

    implementation("androidx.media3:media3-exoplayer:1.4.1")
    api("androidx.media3:media3-session:1.4.1")
}