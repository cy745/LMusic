plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    namespace = "com.lalilu.crash"
    compileSdk = AndroidConfig.COMPILE_SDK_VERSION

    buildFeatures {
        viewBinding = true
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
}

dependencies {
    implementation(libs.startup.runtime)
    implementation(libs.appcompat)
}