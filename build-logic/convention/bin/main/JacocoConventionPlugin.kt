import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.register
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification
import org.gradle.testing.jacoco.tasks.JacocoReport

class JacocoConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("jacoco")

            configure<JacocoPluginExtension> {
                toolVersion = "0.8.11"
            }

            val androidComponents = extensions.findByType(
                com.android.build.api.variant.AndroidComponentsExtension::class.java
            )

            androidComponents?.onVariants { variant ->
                val variantName = variant.name
                val capitalizedVariant = variantName.replaceFirstChar { it.uppercase() }
                val testTaskName = "test${capitalizedVariant}UnitTest"

                tasks.register<JacocoReport>(
                    "jacoco${capitalizedVariant}TestReport"
                ) {
                    dependsOn(testTaskName)
                    group = "Reporting"
                    description = "Generate Jacoco coverage report for $variantName"

                    reports {
                        xml.required.set(true)
                        html.required.set(true)
                        csv.required.set(false)
                    }

                    val fileFilter = listOf(
                        "**/R.class", "**/R$*.class",
                        "**/BuildConfig.*", "**/Manifest*.*",
                        "**/*Test*.*", "android/**/*.*",
                        "**/di/**",
                        "**/*_Factory*",
                        "**/*_MembersInjector*",
                        "**/*Hilt*",
                        "**/databinding/**",
                        "**/*Binding.*",
                        "**/hilt_aggregated_deps/**",
                        "**/*ComposableSingletons*",
                    )

                    classDirectories.setFrom(
                        files(
                            fileTree("${layout.buildDirectory.get()}/tmp/kotlin-classes/$variantName") {
                                exclude(fileFilter)
                            }
                        )
                    )

                    sourceDirectories.setFrom(
                        files("src/main/java", "src/main/kotlin")
                    )

                    executionData.setFrom(
                        fileTree(layout.buildDirectory.get()) {
                            include(
                                "outputs/unit_test_code_coverage/${variantName}UnitTest/**.exec",
                                "jacoco/test${capitalizedVariant}UnitTest.exec"
                            )
                        }
                    )
                }

                tasks.register<JacocoCoverageVerification>(
                    "jacoco${capitalizedVariant}CoverageVerification"
                ) {
                    dependsOn("jacoco${capitalizedVariant}TestReport")
                    group = "Verification"

                    violationRules {
                        rule {
                            limit {
                                minimum = "0.80".toBigDecimal()
                            }
                        }
                    }
                }
            }
        }
    }
}