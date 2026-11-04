plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "tv.hackertube"
    compileSdk {
        version = release(37) { minorApiLevel = 2 }
    }

    defaultConfig {
        // its own id, so it installs next to the English app
        applicationId = "tv.hackertube.zh"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0-zh.1"
    }

    // Release signing comes from the environment (HT_KEYSTORE, HT_KEYSTORE_PASSWORD, HT_KEY_ALIAS,
    // HT_KEY_PASSWORD); without it the release build uses the debug key, as upstream does.
    val keystore = System.getenv("HT_KEYSTORE")
    signingConfigs {
        if (keystore != null) {
            create("release") {
                storeFile = file(keystore)
                storePassword = System.getenv("HT_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("HT_KEY_ALIAS")
                keyPassword = System.getenv("HT_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
            signingConfig = signingConfigs.getByName(if (keystore != null) "release" else "debug")
        }
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2026.09.00"))
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.tv:tv-material:1.1.0")
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.12.0")
    implementation("com.squareup.okhttp3:okhttp:5.5.1")
    implementation("io.coil-kt.coil3:coil-compose:3.6.3")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.6.3")
}
