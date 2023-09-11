plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.lalilu.common"
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
    api(project(":lmedia"))

    // https://github.com/Blankj/AndroidUtilCode/
    // Apache-2.0 License
    // 安卓工具类库
    api("com.blankj:utilcodex:1.31.1")
    api("androidx.palette:palette-ktx:1.0.0")
    api("androidx.core:core-ktx:1.12.0")

    // Koin for Android
    // https://mvnrepository.com/artifact/io.insert-koin/koin-android
    api("io.insert-koin:koin-android:${findProperty("koin_version")}")
    api("io.insert-koin:koin-androidx-compose:${findProperty("koin_compose_version")}")
}