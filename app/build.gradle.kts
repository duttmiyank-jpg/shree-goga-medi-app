plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
    // alias(libs.plugins.google.services) // Disabled until google-services.json is added
}

android {
    namespace = "com.example.miyank"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.miyank"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(project(":shared"))
    implementation("org.jetbrains.compose.runtime:runtime:1.7.1")
    implementation("org.jetbrains.compose.foundation:foundation:1.7.1")
    implementation("org.jetbrains.compose.material3:material3:1.7.1")
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.navigation.compose)
    implementation(libs.kotlinx.serialization.json)
    
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)

    testImplementation("junit:junit:4.13.2")
}
