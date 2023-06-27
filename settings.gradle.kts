rootProject.name = "lmusic"
include(":app")
include(":ui")
include(":common")
include(":lmedia")
include(":lplayer")
include(":crash")

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.aliyun.com/repository/central")
        maven("https://maven.aliyun.com/repository/google")
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven("https://jitpack.io")
        google()
        mavenCentral()
        maven("https://maven.aliyun.com/repository/google")
        maven("https://maven.aliyun.com/repository/central")
    }
}


