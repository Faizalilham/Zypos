plugins {
    `kotlin-dsl`
}

group = "dev.faizal.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11
    }
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(gradleApi())
    compileOnly(libs.ksp.gradlePlugin)
    compileOnly(libs.hilt.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("AndroidLibrary") {
            id = "dev.faizal.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("JvmLibrary") {
            id = "dev.faizal.kotlin.library"
            implementationClass = "JvmLibraryConventionPlugin"
        }
        register("Hilt") {
            id = "dev.faizal.android.hilt"
            implementationClass = "HiltConventionPlugin"
        }
        register("AndroidFeature") {
            id = "dev.faizal.android.feature"
            implementationClass = "AndroidFeatureConventionPlugin"
        }
        register("AndroidCompose") {
            id = "dev.faizal.android.compose"
            implementationClass = "AndroidLibraryComposeConventionPlugin"
        }
        register("AndroidApplication") {
            id = "dev.faizal.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("AndroidApplicationCompose") {
            id = "dev.faizal.android.application.compose"
            implementationClass = "AndroidApplicationComposeConventionPlugin"
        }
    }
}
