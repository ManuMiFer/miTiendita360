import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
    id("kotlin-parcelize")
}

android {
    namespace = "com.miranda.mitiendita360"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.miranda.mitiendita360"
        minSdk = 30
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val localProperties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localProperties.load(FileInputStream(localPropertiesFile))
        }

        buildConfigField(
            "String",
            "API_BASE_URL",
            localProperties.getProperty("API_BASE_URL")
        )
        buildConfigField(
            "String",
            "GOOGLE_VISION_BASE_URL",
            localProperties.getProperty("GOOGLE_VISION_BASE_URL")
        )
        buildConfigField(
            "String",
            "GOOGLE_API_KEY",
            localProperties.getProperty("GOOGLE_API_KEY")
        )
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
        freeCompilerArgs = listOf("-XXLanguage:+PropertyParamAnnotationDefaultTargetMode")
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
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.generativeai)
    implementation(libs.common)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.converter.scalars)
    // CameraX
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    // Iconos
    implementation(libs.androidx.compose.material.icons.extended)
    // Coil para cargar imágenes desde URLs
    implementation(libs.coil.compose)
    // Librería para manejar permisos fácilmente en Compose (Opcional pero muy recomendada)
    implementation(libs.accompanist.permissions)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    // ML Kit Barcode Scanning
    implementation(libs.barcode.scanning)
    // ML Kit Text Recognition
    implementation(libs.text.recognition)

    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    // Dentro del bloque dependencies { ... }
}