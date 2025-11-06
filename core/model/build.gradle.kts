plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    namespace = "com.example.kdyaimap.core.model"
    compileSdk = 36

    defaultConfig {
        minSdk = 28
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("com.google.code.gson:gson:2.10.1")
}