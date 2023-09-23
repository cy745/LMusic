plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.lalilu.ui"
    compileSdk = 34

    defaultConfig {
        minSdk = 21

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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
    // https://github.com/coil-kt/coil
    // Apache-2.0 License
    // 图片加载库
    api("io.coil-kt:coil:${findProperty("coil_version")}")
    api("io.coil-kt:coil-compose:${findProperty("coil_version")}")

    api("androidx.gridlayout:gridlayout:1.0.0")
    api("androidx.constraintlayout:constraintlayout:2.1.4")
    api("androidx.coordinatorlayout:coordinatorlayout:1.2.0")
    api("androidx.recyclerview:recyclerview:1.3.1")

    implementation(project(":common"))
}