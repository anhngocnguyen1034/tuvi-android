import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
}

// Đọc thông tin ký release từ local.properties (KHÔNG commit creds vào git).
val keystoreProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

android {
    namespace = "com.example.tuvi"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.anhnn.tuvi"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            val storeFileName = keystoreProps.getProperty("RELEASE_STORE_FILE")
            if (storeFileName != null) {
                storeFile = file(storeFileName)
                storePassword = keystoreProps.getProperty("RELEASE_STORE_PASSWORD")
                keyAlias = keystoreProps.getProperty("RELEASE_KEY_ALIAS")
                keyPassword = keystoreProps.getProperty("RELEASE_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            // Chỉ ký bằng config release khi có creds (local.properties); CI không có thì để mặc định.
            if (keystoreProps.getProperty("RELEASE_STORE_FILE") != null) {
                signingConfig = signingConfigs.getByName("release")
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    
    // Networking
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization.converter)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    
    // Navigation
    implementation(libs.androidx.navigation.compose)

    // WebView: pull-to-refresh (Compose PullToRefresh không nhận gesture từ WebView)
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    
    // Lifecycle & ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    
    // Serialization
    implementation(libs.kotlinx.serialization.json)

    // Glance — widget màn hình chính (danh ngôn)
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.appcompat)
    implementation(libs.blurview)
    implementation(libs.coil.compose)

    // Firebase Remote Config (giữ lại cho cooldown ads)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.config.ktx)

    // Language picker
    implementation(libs.anhnn.language)

    implementation(libs.anhnn.components.feedback)
    implementation(libs.anhnn.components.rate)
    implementation(libs.anhnn.components.exit)
    implementation(libs.android.blur)

    // AdMob — quản lý qua module anhnn-components-ads (đã api-expose play-services-ads + UMP).
    // Giữ 2 dòng dưới để chắc chắn có sẵn SDK (cùng version, vô hại nếu trùng transitive).
    implementation(libs.anhnn.components.ads)
    implementation(libs.play.services.ads)
    implementation(libs.user.messaging.platform)

    // Analytics (event tracking) — module api-expose firebase-analytics.
    implementation(libs.anhnn.components.analytics)

    // Play Integrity (bảo vệ endpoint AI)
    implementation(libs.play.integrity)

    // Room Database
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
