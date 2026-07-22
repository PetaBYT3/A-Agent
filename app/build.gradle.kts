plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.plugin.serialization)
    //alias(libs.plugins.google.services)
}

android {
    namespace = "com.a.agent"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.a.agent"
        minSdk = 33
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    //androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)

    //Koin Android
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    implementation(libs.koin.androidx.workmanager)

    //Serialization
    implementation(libs.kotlinx.serialization.json)

    //Data Store
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.datastore.core)

    //Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    //Arrow
    implementation(platform(libs.arrow.bom))
    implementation(libs.arrow.core)
    implementation(libs.arrow.fx.coroutine)
    implementation(libs.arrow.optics)
    ksp(libs.arrow.optics.ksp.plugin)

    //FileKit
    implementation(libs.filekit.dialog)
    implementation(libs.filekit.dialog.compose)

    //Coil
    implementation(libs.coil.compose)

    //Zoomable Image Coil
    implementation(libs.zoomable.image.coil)

    //Splash Screen
    implementation(libs.androidx.core.splashscreen)

    //Navigation
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)

    //Icon
    implementation(libs.material.icon.extended)

    //Reorderable
    implementation(libs.reorderable)

    //Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)

    //Ktor
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)

    //CameraX
    implementation(libs.camerax.core)
    implementation(libs.camerax.two)
    implementation(libs.camerax.view)
    implementation(libs.camerax.lifecycle)

    //Tasks Gen AI
    implementation(libs.mediapipe.tasks.genai)
    implementation(libs.mediapipe.tasks.vision)

    //LiteRT LM
    implementation(libs.litert.genai)
}