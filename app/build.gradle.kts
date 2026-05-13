import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.oskolki"
    compileSdk = 36

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.example.Oskolki"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Читаем ключ из secrets.properties
        val secrets = Properties()
        val secretsFile = rootProject.file("secrets.properties")
        if (secretsFile.exists()) {
            secrets.load(FileInputStream(secretsFile))
        }
        val mapsApiKey = secrets.getProperty("MAPS_API_KEY") ?: ""
        val googleClientId = secrets.getProperty("GOOGLE_CLIENT_ID") ?: ""

        buildConfigField("String", "MAPS_API_KEY", "\"$mapsApiKey\"")
        buildConfigField("String", "GOOGLE_CLIENT_ID", "\"$googleClientId\"")
        buildConfigField("String", "BASE_URL", "\"https://oskolki.alexavr.ru/\"")

        // Signing config
        signingConfigs {
            create("release") {
                try {
                    val keystoreProps = Properties()
                    val keystoreFile = rootProject.file("keystore.properties")
                    if (keystoreFile.exists()) {
                        keystoreProps.load(FileInputStream(keystoreFile))
                        storeFile = file(keystoreProps.getProperty("storeFile"))
                        storePassword = keystoreProps.getProperty("storePassword")
                        keyAlias = keystoreProps.getProperty("keyAlias")
                        keyPassword = keystoreProps.getProperty("keyPassword")
                    }
                } catch (e: Exception) {
                    // Signing disabled if keystore.properties not found
                }
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation("com.yandex.android:maps.mobile:4.10.0-lite")
    implementation("com.google.android.material:material:1.11.0")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.camera:camera-core:1.3.0")
    implementation("androidx.camera:camera-camera2:1.3.0")
    implementation("androidx.camera:camera-lifecycle:1.3.0")
    implementation("androidx.camera:camera-view:1.3.0")

    // Network & Coroutines
    implementation(libs.retrofit)
    implementation(libs.okhttp)
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation(libs.coroutines.android)
    implementation(libs.lifecycle.runtime.ktx)

    // ARCore
    implementation(libs.arcore)

    // Glide for images
    implementation(libs.glide)
    implementation("com.github.bumptech.glide:okhttp3-integration:4.15.1")
    
    // ExoPlayer for audio with auth support
    implementation("androidx.media3:media3-exoplayer:1.2.0")
    implementation("androidx.media3:media3-datasource-okhttp:1.2.0")

    // Google Sign-In
    implementation("com.google.android.gms:play-services-auth:21.2.0")

}