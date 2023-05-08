plugins {
    id("android-compose-setup")
}

androidNamespaceExtension("core.ui")

dependencies {
    implementation(libs.bundles.compose)
    implementation(libs.compose.material3)
    implementation(libs.palette)
    implementation(project(":core:data"))
}