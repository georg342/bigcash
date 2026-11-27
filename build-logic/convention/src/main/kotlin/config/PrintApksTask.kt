/*
 * Print APKs task configuration
 * Creates task to print all generated APK paths
 */

import com.android.build.api.variant.AndroidComponentsExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register

/**
 * Configure task to print all APK paths for a project
 * Usage: ./gradlew printApks
 */
internal fun Project.configurePrintApksTask(
    extension: AndroidComponentsExtension<*, *, *>,
) {
    extension.onVariants { variant ->
        val variantName = variant.name.replaceFirstChar { it.uppercase() }
        val apkDirectory = variant.artifacts.get(com.android.build.api.artifact.SingleArtifact.APK)
        tasks.register("print${variantName}Apks") {
            group = "help"
            description = "Prints all APK paths for ${variant.name} variant"
            
            doLast {
                println("APKs for ${variant.name}:")
                val directory = apkDirectory.get().asFile
                println("APK output for ${variant.name}: ${directory.absolutePath}")
                directory.listFiles { file -> file.extension == "apk" }
                    ?.forEach { apk -> println("  - ${apk.absolutePath}") }
            }
        }
    }
}
