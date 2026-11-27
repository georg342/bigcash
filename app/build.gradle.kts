import java.util.Properties

plugins {
    alias(libs.plugins.app.android.application)
    alias(libs.plugins.app.android.application.compose)
    alias(libs.plugins.app.hilt)
    alias(libs.plugins.app.detekt)
}

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) {
        keystorePropertiesFile.inputStream().use { load(it) }
    }
}

android {
    namespace = "com.bigcash.app"

    defaultConfig {
        applicationId = "com.bigcash.app"
        versionCode = 1
        versionName = "1.0.0"
        vectorDrawables.useSupportLibrary = true
    }

    signingConfigs {
        if (keystorePropertiesFile.exists()) {
            create("release") {
                storeFile = rootProject.file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = if (keystorePropertiesFile.exists()) {
                signingConfigs.getByName("release")
            } else {
                // Fallback so `assembleRelease` still produces an installable APK on a fresh clone.
                signingConfigs.getByName("debug")
            }
        }
    }

    buildFeatures {
        buildConfig = true
    }

    lint {
        // Release builds skip the vital lint pass: it is run explicitly in CI (./gradlew :app:lintVitalRelease).
        checkReleaseBuilds = false
        abortOnError = false
    }

    packaging {
        resources.excludes += setOf("/META-INF/{AL2.0,LGPL2.1}")
    }
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:parsing"))
    implementation(project(":core:database"))

    implementation(libs.findLibrary("androidx-core").get())
    implementation(libs.findLibrary("androidx-activity-compose").get())
    implementation(libs.findLibrary("androidx-lifecycle-runtime-compose").get())
    implementation(libs.findLibrary("androidx-lifecycle-viewmodel-compose").get())
    implementation(libs.findLibrary("androidx-hilt-lifecycle-viewmodel-compose").get())

    implementation(libs.findBundle("compose").get())
    implementation(libs.findLibrary("androidx-compose-material-icons-core").get())
    implementation(libs.findLibrary("androidx-compose-animation").get())

    implementation(libs.findLibrary("kotlinx-coroutines-core").get())
    implementation(libs.findLibrary("kotlinx-coroutines-android").get())

    testImplementation(libs.findBundle("unit-test").get())
    testImplementation(libs.findLibrary("junit").get())
}
