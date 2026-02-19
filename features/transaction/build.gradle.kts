plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "dev.faizal.features.transaction"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    dependencies {
        implementation(project(":core:common"))
        implementation(project(":core:designsystem"))
        implementation(project(":core:ui"))
        implementation(project(":core:domain"))
        implementation("io.coil-kt.coil3:coil-compose:3.0.0")

        implementation(platform(libs.androidx.compose.bom))
        implementation(libs.androidx.compose.ui)
        implementation(libs.androidx.compose.material3)
        implementation(libs.androidx.navigation.compose)

        implementation(libs.hilt.android)
        ksp(libs.hilt.compiler)
        implementation(libs.androidx.hilt.navigation.compose)

        implementation(libs.androidx.lifecycle.runtime.ktx)
        implementation(libs.androidx.lifecycle.viewmodel.compose)
        implementation(libs.androidx.hilt.navigation.compose)
    }
}