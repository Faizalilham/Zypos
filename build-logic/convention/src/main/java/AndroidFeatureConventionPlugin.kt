import dev.faizal.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.kotlin
import com.android.build.api.dsl.ApplicationExtension
import dev.faizal.convention.configureKotlinAndroid
import org.gradle.kotlin.dsl.configure

class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply {
                apply("dev.faizal.android.library")
                apply("dev.faizal.android.hilt")
                apply("dev.faizal.android.compose")
            }

            dependencies {
                add("implementation", project(":core:common"))
                add("implementation", project(":core:designsystem"))
                add("implementation", project(":core:ui"))
                add("implementation", project(":core:domain"))
                add("testImplementation", kotlin("test"))
                add("androidTestImplementation", kotlin("test"))

                add("implementation", libs.findLibrary("androidx.hilt.navigation.compose").get())
                add("implementation", libs.findLibrary("androidx.lifecycle.runtime.ktx").get())
                add("implementation", libs.findLibrary("androidx.lifecycle.viewmodel.compose").get())
                add("implementation", libs.findLibrary("kotlinx.coroutines.android").get())
                add("implementation", libs.findLibrary("androidx.navigation.compose").get())
                add("implementation", libs.findLibrary("coil.compose").get())
                add("implementation", libs.findLibrary("androidx.compose.material.icons").get())
            }
        }
    }
}
