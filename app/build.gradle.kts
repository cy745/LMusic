import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Properties
import java.util.TimeZone

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-android")
    id("com.google.devtools.ksp")
}

val keystoreProps = rootProject.file("keystore.properties")
    .takeIf { it.exists() }
    ?.let { Properties().apply { load(FileInputStream(it)) } }

fun releaseTime(): String = SimpleDateFormat("yyyyMMdd_HHmmZ").run {
    timeZone = TimeZone.getTimeZone("Asia/Shanghai")
    format(Date())
}

kotlin {
    jvmToolchain(8)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

android {
    namespace = "com.lalilu"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.lalilu.lmusic"
        minSdk = 21
        targetSdk = 33
        versionCode = 42
        versionName = "1.5.4"

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildFeatures {
        compose = true
        viewBinding = true
        buildConfig = true
    }

    if (keystoreProps != null) {
        val storeFileValue = keystoreProps["storeFile"]?.toString() ?: ""
        val storePasswordValue = keystoreProps["storePassword"]?.toString() ?: ""
        val keyAliasValue = keystoreProps["keyAlias"]?.toString() ?: ""
        val keyPasswordValue = keystoreProps["keyPassword"]?.toString() ?: ""

        if (storeFileValue.isNotBlank() && file(storeFileValue).exists()) {
            signingConfigs.create("release") {
                storeFile(file(storeFileValue))
                storePassword(storePasswordValue)
                keyAlias(keyAliasValue)
                keyPassword(keyPasswordValue)
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = kotlin.runCatching { signingConfigs["release"] }.getOrNull()
            resValue("string", "app_name", "@string/app_name_release")
        }

        create("beta") {
            isMinifyEnabled = true
            isShrinkResources = true

            versionNameSuffix = "-BETA_${releaseTime()}"
            applicationIdSuffix = ".beta"
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = kotlin.runCatching { signingConfigs["release"] }.getOrNull()
            resValue("string", "app_name", "@string/app_name_beta")

            matchingFallbacks.add("release")
            matchingFallbacks.add("debug")
        }

        debug {
            versionNameSuffix = "-DEBUG_${releaseTime()}"
            applicationIdSuffix = ".debug"
            signingConfig = signingConfigs.getByName("debug")

            resValue("string", "app_name", "@string/app_name_debug")
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
        kotlinCompilerExtensionVersion = findProperty("compose_compiler_version").toString()
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(project(":common"))
    implementation(project(":ui"))
    implementation(project(":lplayer"))
    implementation(project(":crash"))

    // compose
    implementation("androidx.compose.compiler:compiler:${findProperty("compose_compiler_version")}")
    implementation(platform("androidx.compose:compose-bom:${findProperty("compose_bom_version")}"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-util")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-viewbinding")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material3:material3-window-size-class")
    implementation("androidx.compose.runtime:runtime-livedata")
    implementation("androidx.compose.material:material")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-tooling-preview")

    // compose-destinations
    implementation("io.github.raamcosta.compose-destinations:animations-core:${findProperty("compose_destinations_version")}")
    ksp("io.github.raamcosta.compose-destinations:ksp:${findProperty("compose_destinations_version")}")


    // accompanist
    // https://google.github.io/accompanist
    implementation("com.google.accompanist:accompanist-navigation-animation:${findProperty("accompanist_version")}")
    implementation("com.google.accompanist:accompanist-systemuicontroller:${findProperty("accompanist_version")}")
    implementation("com.google.accompanist:accompanist-flowlayout:${findProperty("accompanist_version")}")
    implementation("com.google.accompanist:accompanist-permissions:${findProperty("accompanist_version")}")

    // lottie
    // https://mvnrepository.com/artifact/com.airbnb.android/lottie-compose
    implementation("com.airbnb.android:lottie-compose:5.2.0")

    // https://github.com/Block-Network/StatusBarApiExample
    // 墨 · 状态栏歌词 API
    implementation("com.github.577fkj:StatusBarApiExample:v2.0")

    // https://github.com/aclassen/ComposeReorderable
    // https://mvnrepository.com/artifact/org.burnoutcrew.composereorderable/reorderable
    // Apache-2.0 license
    // Compose的拖动排序组件
    implementation("org.burnoutcrew.composereorderable:reorderable:0.9.6")
    // 0.9.2对LazyColumn的ContentPadding存在偏移的bug

    // https://gitee.com/simplepeng/SpiderMan
    // Apache-2.0 License
    // 闪退崩溃日志捕获库
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.10")

    // https://github.com/square/retrofit
    // https://mvnrepository.com/artifact/com.squareup.retrofit2/retrofit
    // Apache-2.0 License
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // https://github.com/Commit451/NativeStackBlur
    // Apache-2.0 License
    // Bitmap的Blur实现库
    implementation("com.github.Commit451:NativeStackBlur:1.0.4")

    // https://github.com/coil-kt/coil
    // Apache-2.0 License
    // 图片加载库
    implementation("io.coil-kt:coil:${findProperty("coil_version")}")
    implementation("io.coil-kt:coil-compose:${findProperty("coil_version")}")

    // https://github.com/Moriafly/LyricViewX
    // GPL-3.0 License
    // 歌词组件
    implementation("com.github.cy745:LyricViewX:7c92c6d19a")

    // https://github.com/qinci/EdgeTranslucent
    // https://github.com/cy745/EdgeTranslucent
    // Undeclared License
    // 实现边沿渐变透明
    implementation("com.github.cy745:EdgeTranslucent:8c25866a14")

    implementation("com.github.angcyo:DslAdapter:6.0.1")

    // Koin for Android
    // https://mvnrepository.com/artifact/io.insert-koin/koin-android
    implementation("io.insert-koin:koin-android:${findProperty("koin_version")}")
    implementation("io.insert-koin:koin-androidx-compose:${findProperty("koin_compose_version")}")

    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation("androidx.activity:activity-compose:1.7.2")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.gridlayout:gridlayout:1.0.0")
    implementation("androidx.recyclerview:recyclerview:1.3.1-rc01")
    implementation("androidx.dynamicanimation:dynamicanimation:1.1.0-alpha03")
    implementation("androidx.navigation:navigation-compose:${findProperty("navigation_version")}")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:${findProperty("lifecycle_version")}")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:${findProperty("lifecycle_version")}")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:${findProperty("lifecycle_version")}")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:${findProperty("lifecycle_version")}")
}