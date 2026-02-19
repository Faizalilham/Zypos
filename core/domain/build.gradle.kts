plugins {
    id("dev.faizal.android.library")
}

android {
    namespace = "dev.faizal.core.domain"
}

dependencies {
    implementation(libs.kotlinx.coroutines.android)
}
