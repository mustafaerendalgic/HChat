// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    id("com.google.devtools.ksp") version "2.2.10-2.0.2"
}
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.google.gms:google-services:4.4.4")
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.56")
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.7.7")
        classpath(kotlin("gradle-plugin", version = "2.2.10"))
    }
}