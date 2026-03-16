plugins {
    id("dev.faizal.android.library")
    id("dev.faizal.android.hilt")
    id("dev.faizal.convention.jacoco")
}

android {
    namespace = "dev.faizal.core.data"

    defaultConfig {
        testInstrumentationRunner =  "androidx.test.runner.AndroidJUnitRunner"
    }
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:common"))

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.core.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.kotlinx.coroutines.android)

    api(libs.sqlcipher.android)
    api(libs.androidx.sqlite.ktx)
    api(libs.androidx.security.crypto)
    
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
}