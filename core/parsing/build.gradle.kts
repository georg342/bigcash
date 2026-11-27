plugins {
    alias(libs.plugins.app.jvm.library)
    alias(libs.plugins.app.detekt)
}

dependencies {
    implementation(project(":core:model"))
    testImplementation(libs.findLibrary("junit").get())
    testImplementation(libs.findLibrary("google-truth").get())
}
