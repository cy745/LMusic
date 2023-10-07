import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Properties
import java.util.TimeZone

plugins {
    id("com.android.application")
    kotlin("android")
    id("com.google.devtools.ksp")
}

val keystoreProps = rootProject.file("keystore.properties")
    .takeIf { it.exists() }
    ?.let { Properties().apply { load(FileInputStream(it)) } }

fun releaseTime(): String = SimpleDateFormat("yyyyMMdd_HHmmZ").run {
    timeZone = TimeZone.getTimeZone("Asia/Shanghai")
    format(Date())
}


android {
    namespace = "com.lalilu"
    compileSdk = AndroidConfig.COMPILE_SDK_VERSION

    defaultConfig {
        applicationId = "com.lalilu.lmusic"
        minSdk = AndroidConfig.MIN_SDK_VERSION
        targetSdk = AndroidConfig.TARGET_SDK_VERSION
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
                ?: signingConfigs.getByName("debug")
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.compose.compiler.get().version.toString()
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    lint {
        disable += "Instantiatable"
    }
}

dependencies {
    implementation(project(":ui"))
    implementation(project(":lplayer"))
    implementation(project(":crash"))
    implementation(project(":extension-core"))
    ksp(project(":extension-ksp"))

    // compose-destinations
    implementation(libs.compose.destinations.animations.core)
    ksp(libs.compose.destinations.ksp)

    // accompanist
    // https://google.github.io/accompanist
    implementation(libs.bundles.accompanist)

    implementation(libs.activity.compose)
    implementation(libs.navigation.compose)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.livedata.ktx)

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

    // https://github.com/Moriafly/LyricViewX
    // GPL-3.0 License
    // 歌词组件
    implementation("com.github.cy745:LyricViewX:7c92c6d19a")

    // https://github.com/qinci/EdgeTranslucent
    // https://github.com/cy745/EdgeTranslucent
    // Undeclared License
    // 实现边沿渐变透明
    implementation("com.github.cy745:EdgeTranslucent:8c25866a14")

    debugImplementation("com.github.getActivity:Logcat:11.8")
}