plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services") version "4.4.2" apply false
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.apparduino"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.apparduino"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation("com.loopj.android:android-async-http:1.4.10") // Agregar LoopJ aquí
    implementation("androidx.appcompat:appcompat:1.3.1")


    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))

    implementation("com.google.firebase:firebase-analytics")

    implementation("com.google.firebase:firebase-database")
    implementation("androidx.appcompat:appcompat:1.5.1")
    implementation("com.android.volley:volley:1.2.1")
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.activity.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.material3)  // Agregar Volley
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.ui.test.junit4)
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)
}

apply(plugin = "com.google.gms.google-services")
