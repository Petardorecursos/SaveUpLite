plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.saveuplite"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.saveuplite"
        minSdk = 24
        targetSdk = 36
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
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    // Material 3
    implementation("androidx.compose.material3:material3:1.2.0")
    // Window Size Class (parte de material3-window-size)
    implementation("androidx.compose.material3:material3-window-size-class:1.2.0")
    // NAVEGACIÓN
    implementation("androidx.navigation:navigation-compose:2.7.5")

    // Accompanist Navigation Animation
    implementation("com.google.accompanist:accompanist-navigation-animation:0.34.0")

    // Location services
    implementation("com.google.android.gms:play-services-location:21.3.0")

    // En app/build.gradle.kts

    // ... otras dependencias
    implementation("androidx.core:core-ktx:1.13.1") // Asegúrate de tener las básicas
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.3")
    implementation("androidx.activity:activity-compose:1.9.0")
    // ...

    // --- DEPENDENCIAS DE GOOGLE MAPS ---
    // 1. Añade la librería de Maps para Compose
    implementation("com.google.maps.android:maps-compose:4.4.1")

    // 2. Añade la librería de servicios de ubicación (ya la deberías tener por el FusedLocationProvider)
    implementation("com.google.android.gms:play-services-location:21.3.0")
    // --- FIN DEPENDENCIAS DE GOOGLE MAPS ---

}


    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

}
