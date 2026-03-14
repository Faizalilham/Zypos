plugins {
    id("dev.faizal.android.library")
    id("dev.faizal.android.compose")
    id("dev.faizal.android.hilt")
}

android {
    namespace = "dev.faizal.core.common"
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:designsystem"))

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
}