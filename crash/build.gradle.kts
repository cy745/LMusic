plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    namespace = "com.lalilu.crash"
    compileSdk = libs.versions.compile.version.get().toIntOrNull()

    defaultConfig {
        minSdk = libs.versions.min.sdk.version.get().toIntOrNull()
    }

    buildFeatures {
        viewBinding = true
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
    implementation(libs.startup.runtime)
    implementation(libs.appcompat)
    implementation(libs.utilcodex)
}