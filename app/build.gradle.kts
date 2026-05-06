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
        versionCode = 5
        versionName = "1.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = file(System.getenv("KEYSTORE_FILE") ?: "release.keystore")
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: ""
            keyAlias = System.getenv("KEY_ALIAS") ?: ""
            keyPassword = System.getenv("KEY_PASSWORD") ?: ""
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
                rootProject.file("proguard/proguard-base.pro"),
                rootProject.file("proguard/proguard-gson.pro"),
                rootProject.file("proguard/proguard-coroutines.pro"),
            )
        }
    }

    flavorDimensions += "environment"

    productFlavors {
        create("development") {
            dimension = "environment"
            applicationIdSuffix = ".dev"
        }

        create("staging") {
            dimension = "environment"
            applicationIdSuffix = ".staging"
        }

        create("production") {
            dimension = "environment"
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
    implementation(project(":features:settlement"))
    implementation(project(":features:onboarding"))

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.hilt.navigation.compose)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.ui.tooling.preview)
    debugImplementation(libs.androidx.ui.tooling)
    implementation(libs.coil.compose)
    implementation(libs.app.update)
    implementation(libs.app.update.ktx)

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.itext.core)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.mockk)
    testImplementation(libs.truth)

    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.androidx.room.testing)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.truth)
    androidTestImplementation(libs.turbine)

    implementation("com.google.errorprone:error_prone_annotations:2.23.0")
}