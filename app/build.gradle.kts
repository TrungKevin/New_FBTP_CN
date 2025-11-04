plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    // alias(libs.plugins.dagger.hilt)
    // id("com.google.devtools.ksp") version "2.0.21-1.0.27"
    // THÊM PLUGIN GOOGLE SERVICES
    id("com.google.gms.google-services")
}

android {
    namespace = "com.trungkien.fbtp_cn"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.trungkien.fbtp_cn"
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
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    
    lint {
        baseline = file("lint-baseline.xml")
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-text")
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation("androidx.compose.material:material-icons-extended")
    implementation(libs.play.services.location)
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")
    implementation("androidx.navigation:navigation-compose:2.9.2")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    // Removed Google Maps dependencies - using OpenStreetMap (OSMDroid) instead
    implementation(libs.retrofit2)
    implementation(libs.retrofit2.converter.gson)
    implementation(libs.gson)
    implementation(libs.coil.compose)
    // For HorizontalPager (foundation) and coroutines used by tab syncing
    implementation(libs.androidx.foundation)
    implementation(libs.coroutines.android)
    
    // Google Maps SDK
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    
    // OpenStreetMap support (keep for fallback)
    implementation("org.osmdroid:osmdroid-android:6.1.18")
    
    // Geocoding support for address to coordinates conversion
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    
    // THÊM FIREBASE DEPENDENCIES
    implementation(platform("com.google.firebase:firebase-bom:33.1.2"))
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")
    implementation("com.google.firebase:firebase-messaging-ktx")
    
    // IMAGE PICKER DEPENDENCIES
    implementation("com.github.dhaval2404:imagepicker:2.1")
    
    // implementation(libs.hilt.android)
    // ksp(libs.hilt.compiler)
    // implementation(libs.androidx.hilt.navigation.compose)
}