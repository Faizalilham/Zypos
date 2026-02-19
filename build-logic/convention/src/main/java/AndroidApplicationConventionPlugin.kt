import com.android.build.api.dsl.ApplicationExtension
import dev.faizal.convention.configureKotlinAndroid
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.application")
                apply("org.jetbrains.kotlin.android")
                apply("dev.faizal.android.hilt")
            }


            extensions.configure<ApplicationExtension> {
                configureKotlinAndroid(this)
                defaultConfig.targetSdk = 35
                compileOptions {
                    isCoreLibraryDesugaringEnabled = true
                }
            }
        }
    }

}