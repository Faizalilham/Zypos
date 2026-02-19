plugins {
    id("dev.faizal.android.feature")
}

android {
    namespace = "dev.faizal.features.favorite"
}

dependencies {
    implementation(project(":features:order"))
}