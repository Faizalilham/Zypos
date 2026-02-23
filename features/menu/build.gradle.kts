plugins {
    id("dev.faizal.android.feature")
    id("dev.faizal.convention.jacoco")
}

android {
    namespace = "dev.faizal.features.menu"
}

dependencies {
    implementation(project(":features:order"))

    // Unit Testing
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.mockk)
    testImplementation(libs.truth)
    testImplementation(libs.androidx.core.testing)
}