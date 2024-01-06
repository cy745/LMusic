plugins {
    alias(libs.plugins.library)
    alias(libs.plugins.kotlin)
}

android {
    namespace = "com.lalilu.value_cat"
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
    implementation("com.github.getActivity:EasyWindow:10.6")
    implementation(libs.startup.runtime)
}