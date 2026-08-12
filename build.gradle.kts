// Top-level build file where you can add configuration options common to all sub-projects/modules.
// AGP/databinding kéo javapoet 1.10.0 (thiếu ClassName.canonicalName()) vào plugin classpath
// cùng với javapoet 1.13.0 của Hilt → NoSuchMethodError trong hiltAggregateDepsDebug.
// Force về 1.13.0 để chỉ còn một bản trên classpath.
buildscript {
    configurations.all {
        resolutionStrategy.force("com.squareup:javapoet:1.13.0")
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
}
