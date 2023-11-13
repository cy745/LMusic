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

    api(project(":lmedia"))
    api(project(":common"))

    api(libs.coil)
    api(libs.coil.compose)

    // https://github.com/aclassen/ComposeReorderable
    // https://mvnrepository.com/artifact/org.burnoutcrew.composereorderable/reorderable
    // Apache-2.0 license
    // Compose的拖动排序组件
    api("org.burnoutcrew.composereorderable:reorderable:0.9.6")
    // 0.9.2对LazyColumn的ContentPadding存在偏移的bug

    implementation(libs.activity.compose)
    // compose
    api(libs.compose.compiler)
    api(platform(libs.compose.bom))
    api(libs.bundles.compose)
    debugApi(libs.bundles.compose.debug)
}