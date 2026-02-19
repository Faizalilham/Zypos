plugins {
    id("dev.faizal.android.library")
    id("dev.faizal.android.compose")
}

android {
    namespace = "dev.faizal.core.designsystem"
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
}