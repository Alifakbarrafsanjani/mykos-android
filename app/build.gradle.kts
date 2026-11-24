plugins {
    alias(libs.plugins.android.application)
    id("com.android.legacy-kapt") version libs.versions.agp.get()
    id("com.google.gms.google-services")
}

android {
    namespace = "com.alif.mykos"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.alif.mykos"
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

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)
    implementation(libs.material)
    implementation("androidx.activity:activity-ktx:1.10.1")
    implementation(libs.androidx.constraintlayout)

    // Navigation Component (Fragment & UI)
    implementation("androidx.navigation:navigation-fragment-ktx:2.8.9")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.9")

    // DataStore Preferences (Pengaturan Tema)
    implementation("androidx.datastore:datastore-preferences:1.1.4")

    // Coil (Memuat foto kamar dari kamera/galeri secara ringan)
    implementation("io.coil-kt:coil:2.7.0")

    // Firebase BoM & Authentication
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-auth")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}