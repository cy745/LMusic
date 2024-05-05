plugins {
    id("kotlin")
}

dependencies {
    implementation("com.google.devtools.ksp:symbol-processing-api:1.9.20-1.0.14")
    implementation("com.squareup:kotlinpoet-ksp:1.14.2")
    implementation(project(":voyager-router-annotation"))
}