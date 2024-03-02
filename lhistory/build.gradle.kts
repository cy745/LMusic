plugins {
    id("com.android.library")
    kotlin("android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.lalilu.lhistory"
    compileSdk = AndroidConfig.COMPILE_SDK_VERSION

    buildFeatures {
        compose = true
    }
    defaultConfig {
        minSdk = AndroidConfig.MIN_SDK_VERSION

        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
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
    implementation(libs.room.ktx)
    implementation(libs.room.runtime)
    ksp(libs.room.compiler)

    implementation(project(":component"))
}