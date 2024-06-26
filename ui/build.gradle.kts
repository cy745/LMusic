plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    namespace = "com.lalilu.ui"
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
    api(libs.gridlayout)
    api(libs.constraintlayout)
    api(libs.coordinatorlayout)
    api(libs.recyclerview)

    implementation(project(":common"))
}