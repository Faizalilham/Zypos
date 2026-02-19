plugins {
    id("dev.faizal.android.library")
    id("dev.faizal.android.compose")
    alias(libs.plugins.serialization)
}

android {
    namespace = "dev.faizal.core.ui"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:designsystem"))

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.kotlin.serialization)
}