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
                buildTypes {
                    getByName("release") {
                        isMinifyEnabled = true
                        isShrinkResources = true
                        proguardFiles(
                            getDefaultProguardFile("proguard-android-optimize.txt"),
                            rootProject.file("proguard/proguard-base.pro"),
                            rootProject.file("proguard/proguard-gson.pro"),
                            rootProject.file("proguard/proguard-coroutines.pro"),
                        )
                    }
                }
                configureKotlinAndroid(this)
                defaultConfig.targetSdk = 35
                compileOptions {
                    isCoreLibraryDesugaringEnabled = true
                }
            }
        }
    }

}