plugins {
    alias(libs.plugins.app.android.library)
    alias(libs.plugins.app.hilt)
    alias(libs.plugins.app.android.room)
    alias(libs.plugins.app.detekt)
}

android {
    namespace = "com.bigcash.core.database"
}

dependencies {
    implementation(project(":core:model"))
    implementation(libs.findLibrary("kotlin-stdlib").get())
    implementation(libs.findLibrary("androidx-core").get())
    implementation(libs.findLibrary("kotlinx-coroutines-core").get())
    implementation(libs.findLibrary("kotlinx-coroutines-android").get())
}
