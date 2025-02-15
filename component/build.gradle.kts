import org.jetbrains.kotlin.compose.compiler.gradle.ComposeFeatureFlag

plugins {
    id("com.android.library")
    kotlin("android")
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.lalilu.component"
    compileSdk = libs.versions.compile.version.get().toIntOrNull()

    defaultConfig {
        minSdk = libs.versions.min.sdk.version.get().toIntOrNull()
    }

    buildFeatures {
        compose = true
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

composeCompiler {
    featureFlags.set(
        listOf(
            ComposeFeatureFlag.StrongSkipping
        )
    )
}

dependencies {
    // compose
    api(platform(libs.compose.bom.alpha))
    api(libs.activity.compose)
    api(libs.bundles.compose)
    api(libs.bundles.compose.debug)

    // accompanist
    // https://google.github.io/accompanist
    api(libs.bundles.accompanist)
    api(libs.bundles.voyager)
    api(libs.bundles.coil3)
    api(libs.lottie.compose)
    api(libs.human.readable)
    api(libs.kotlinx.datetime)
    api(libs.remixicon.kmp)

    api(project(":lmedia"))
    api(project(":common"))
    api(project(":lplayer"))

    // https://github.com/Calvin-LL/Reorderable
    // Apache-2.0 license
    api("sh.calvin.reorderable:reorderable:2.4.0")
    api("com.github.cy745:AnyPopDialog-Compose:cb92c5b6dc")
    api("me.rosuh:AndroidFilePicker:1.0.1")
    api("com.cheonjaeung.compose.grid:grid:2.0.0")
    api("com.github.nanihadesuka:LazyColumnScrollbar:2.2.0")
    api("com.github.GIGAMOLE:ComposeFadingEdges:1.0.4")
    api("dev.chrisbanes.haze:haze:1.2.2")
    api("dev.chrisbanes.haze:haze-materials:1.2.2")

    api("io.github.FunnySaltyFish:data-saver-core:1.2.2")

    // https://mvnrepository.com/artifact/org.jetbrains.androidx.navigation/navigation-compose
    api("org.jetbrains.androidx.navigation:navigation-compose:2.8.0-alpha10")
    api("androidx.compose.material3:material3-adaptive-navigation-suite")
}