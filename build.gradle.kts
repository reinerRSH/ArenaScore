// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.kotlinAndroid) apply false
    alias(libs.plugins.kotlinCompose) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    id("org.jetbrains.kotlin.multiplatform") version "2.1.0" apply false
    id("com.android.library") version "8.13.2" apply false
    id("org.jetbrains.compose") version "1.7.0" apply false
    id("com.google.gms.google-services") version "4.4.4" apply false
}