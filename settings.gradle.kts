pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven("https://jitpack.io")
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}


rootProject.name = "LMusic"
include(":app")
include(":ui")
include(":common")
include(":lmedia")
include(":lplayer")
include(":lplaylist")
include(":lhistory")
include(":lartist")
include(":lalbum")
include(":ldictionary")
include(":crash")
include(":component")