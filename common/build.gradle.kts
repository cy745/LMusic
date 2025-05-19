plugins {
    alias(libs.plugins.library)
    alias(libs.plugins.kotlin)
}

android {
    namespace = "com.lalilu.common"
    compileSdk = libs.versions.compile.version.get().toIntOrNull()

    defaultConfig {
        minSdk = libs.versions.min.sdk.version.get().toIntOrNull()
    }
    buildTypes {
        release {
            consumerProguardFiles("proguard-rules.pro")
        }
    }
}

dependencies {
    api(libs.utilcodex)
    api(libs.gson)
    api(libs.appcompat)
    api(libs.core.ktx)
    api(libs.palette.ktx)
    api(libs.dynamicanimation.ktx)
    api(libs.media)

    api(libs.kotlinx.coroutines.guava)

    api(libs.bundles.koin)
    api(libs.krouter.core)
}