plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.dagger.hilt.android)
    alias(libs.plugins.compose.compiler)
    kotlin("plugin.serialization") version "1.9.20"
    id("com.google.gms.google-services")

    // Preview 오류 -> 컴포즈 컴파일러 버전 명지 (BOM과 버전 맞추기)
    //id("org.jetbrains.kotlin.plugin.compose.compiler") version "1.5.10"
}

android {
    namespace = "com.lago.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.lago.app"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    
    // Firebase BOM
    implementation(platform("com.google.firebase:firebase-bom:33.1.2"))
    implementation("com.google.firebase:firebase-messaging-ktx")
    
    // Compose BOM
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation(libs.androidx.lifecycle.runtime.compose.android)
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    
    // Hilt
    implementation("com.google.dagger:hilt-android:2.48")
    kapt("com.google.dagger:hilt-compiler:2.48")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    
    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    
    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    
    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.6")

    // Vico Charts
    implementation("com.patrykandpatrick.vico:compose:1.13.1")
    implementation("com.patrykandpatrick.vico:compose-m3:1.13.1")
    implementation("com.patrykandpatrick.vico:core:1.13.1")


    // Vico Charts
    implementation("com.patrykandpatrick.vico:compose:1.13.1")
    implementation("com.patrykandpatrick.vico:compose-m3:1.13.1")
    implementation("com.patrykandpatrick.vico:core:1.13.1")


    // TradingView Lightweight Charts (로컬 모듈)
    implementation(project(":lightweightlibrary"))

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")

    // Kotlin Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    // FlexibleBottomSheet Material3
    implementation("com.github.skydoves:flexible-bottomsheet-material3:0.1.5")

    // MotionLayout for Compose
    implementation("androidx.constraintlayout:constraintlayout-compose:1.0.1")

    // Lottie for Compose
    implementation("com.airbnb.android:lottie-compose:6.4.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.02.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}