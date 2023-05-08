plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    mavenLocal()
    google()
}

dependencies {
    //   implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.21")
    implementation(libs.plugins.kotlin.plugin.get().toString())
    implementation(libs.plugins.agp.get().toString())
}

kotlin {
    sourceSets.getByName("main").kotlin.srcDir("buildSrc/src/main/kotlin")
}