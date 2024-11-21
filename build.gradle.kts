// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
}

buildscript {
    repositories {
        google()          // For Google, Android SDKs, and Firebase-related repositories
        mavenCentral()    // For other standard dependency artifacts
    }
    dependencies {
        // Safe Args plugin
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.5.3")
    }
}