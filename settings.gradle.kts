rootProject.name = "lmusic"
include(":app")
include(":ui")
include(":common")
include(":lmedia")
include(":lplayer")
include(":crash")

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.aliyun.com/repository/central")
        maven("https://maven.aliyun.com/repository/google")
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://maven.aliyun.com/repository/google")
        maven("https://maven.aliyun.com/repository/central")
        maven("https://jitpack.io")
    }
}


val extFile = File(rootDir, "extension")
if (extFile.exists() && extFile.isDirectory) {
    include(":extension")
}
