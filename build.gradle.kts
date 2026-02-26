// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.serialization) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.jetbrains.kotlin.jvm) apply false
    alias(libs.plugins.sonarqube) apply true
}
true


subprojects {
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        kotlinOptions {
            val directory = File(rootProject.projectDir, "compose_compiler_reports").absolutePath
            if (project.findProperty("composeCompilerReports") == "true") {
                freeCompilerArgs += listOf(
                    "-P",
                    "plugin:androidx.compose.compiler.plugins.kotlin:stabilityConfigurationPath=${rootProject.projectDir}/stability-config.txt"
                )
                freeCompilerArgs += listOf(
                    "-P",
                    "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=${directory}/compose_compiler"
                )
            }
            if (project.findProperty("composeCompilerMetrics") == "true") {
                freeCompilerArgs += listOf(
                    "-P",
                    "plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=${directory}/compose_compiler"
                )
            }
        }
    }
}

sonarqube {
    properties {
        property("sonar.projectKey", "Faizalilham_Zypos")
        property("sonar.organization", "faizalilham")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.qualitygate.wait", "true")
        property("sonar.java.coveragePlugin", "jacoco")

        // Tambahkan ini ↓
        property("sonar.sources", "src/main/java,src/main/kotlin")
        property("sonar.tests", "src/test/java,src/test/kotlin")

        property(
            "sonar.coverage.jacoco.xmlReportPaths",
            listOf(
                "${rootProject.projectDir}/core/data/build/reports/jacoco/jacocoDebugTestReport/jacocoDebugTestReport.xml",
            ).joinToString(",")
        )

        // Exclude UI dari coverage check
        property(
            "sonar.coverage.exclusions",
            "**/ui/screens/**,**/ui/components/**,**/*Screen*,**/di/**,**/*Module*"
        )
    }
}

tasks.register("jacocoAllModulesReport") {
    group = "Reporting"
    description = "Generate JaCoCo reports for all modules"

    dependsOn(
        subprojects.flatMap { subproject ->
            subproject.tasks.matching { it.name == "jacocoDebugTestReport" }
        }
    )
}

tasks.register("jacocoAllModulesVerification") {
    group = "Verification"
    description = "Verify coverage for all modules"

    dependsOn(
        subprojects.flatMap { subproject ->
            subproject.tasks.matching { it.name == "jacocoDebugCoverageVerification" }
        }
    )
}