plugins {
    id("dev.faizal.android.application")
    id("dev.faizal.android.application.compose")
    id("dev.faizal.android.hilt")
    alias(libs.plugins.serialization)
}

android {
    namespace = "dev.faizal.zypos"

    defaultConfig {
        applicationId = "dev.faizal.zypos"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }
}

dependencies {
    coreLibraryDesugaring(libs.android.desugarJdkLibs)

    implementation(project(":core:common"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:ui"))
    implementation(project(":core:data"))

    implementation(project(":features:dashboard"))
    implementation(project(":features:favorite"))
    implementation(project(":features:menu"))
    implementation(project(":features:order"))
    implementation(project(":features:transaction"))

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.hilt.navigation.compose)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.ui.tooling.preview)
    debugImplementation(libs.androidx.ui.tooling)
    implementation(libs.coil.compose)

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.itext.core)
}