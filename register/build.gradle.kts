plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

repositories {
    google()
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation("org.ow2.asm:asm-util:9.2")
    implementation("com.android.tools.build:gradle-api:8.2.0-rc02")
}

gradlePlugin {
    plugins.create("RegisterPlugin") {
        id = "com.lalilu.register"
        implementationClass = "com.lalilu.register.RegisterPlugin"
    }
}