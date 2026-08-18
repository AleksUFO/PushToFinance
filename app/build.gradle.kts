import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

// Optional signing configuration.
//
// Create a `keystore.properties` file in the project root (it is gitignored):
//   storeFile=../my-release.keystore
//   storePassword=...
//   keyAlias=...
//   keyPassword=...
//
// Alternatively the same values can be provided as environment variables:
//   KEYSTORE_FILE, KEYSTORE_STORE_PASSWORD, KEYSTORE_KEY_ALIAS, KEYSTORE_KEY_PASSWORD
//
// If no keystore is configured, the release build is produced unsigned.
val keystoreProps = Properties()
val keystorePropsFile = rootProject.file("keystore.properties")
if (keystorePropsFile.exists()) keystorePropsFile.inputStream().use { keystoreProps.load(it) }

val keystoreFile = keystoreProps.getProperty("storeFile") ?: System.getenv("KEYSTORE_FILE")
val keystoreStorePassword = keystoreProps.getProperty("storePassword") ?: System.getenv("KEYSTORE_STORE_PASSWORD")
val keystoreKeyAlias = keystoreProps.getProperty("keyAlias") ?: System.getenv("KEYSTORE_KEY_ALIAS")
val keystoreKeyPassword = keystoreProps.getProperty("keyPassword") ?: System.getenv("KEYSTORE_KEY_PASSWORD")
val hasKeystore = !keystoreFile.isNullOrBlank()

android {
    namespace = "com.pushtofinance.infinapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.pushtofinance.infinapp"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
    }

    signingConfigs {
        if (hasKeystore) {
            create("release") {
                storeFile = file(keystoreFile!!)
                storePassword = keystoreStorePassword ?: ""
                keyAlias = keystoreKeyAlias ?: ""
                keyPassword = keystoreKeyPassword ?: ""
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            if (hasKeystore) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
        debug {
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.okhttp)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.core.splashscreen)
    debugImplementation(libs.androidx.compose.ui.tooling)
}