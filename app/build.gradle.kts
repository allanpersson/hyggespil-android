plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

/* Versionen kommer fra spillets manifest.json (sendes ind som -PversionName=x.y.z).
   versionCode laves af x.y.z, så hver udgivelse kan opdateres på Android. */
val spillVersion = (findProperty("versionName") as String?) ?: "0.0.0"
val dele = (0..3).map { spillVersion.split(".").getOrNull(it)?.toIntOrNull() ?: 0 }
val spillKode = dele[0] * 1000000 + dele[1] * 10000 + dele[2] * 100 + dele[3]

android {
    namespace = "dk.allanpersson.hyggespil"
    compileSdk = 34

    defaultConfig {
        applicationId = "dk.allanpersson.hyggespil"
        minSdk = 24
        targetSdk = 34
        versionCode = spillKode
        versionName = spillVersion
        resourceConfigurations += listOf("da", "en")
    }

    signingConfigs {
        create("udgivelse") {
            val fil = System.getenv("ANDROID_KEYSTORE")
            if (!fil.isNullOrBlank() && File(fil).exists()) {
                storeFile = File(fil)
                storePassword = System.getenv("ANDROID_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("ANDROID_KEY_ALIAS")
                keyPassword = System.getenv("ANDROID_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = if (!System.getenv("ANDROID_KEYSTORE").isNullOrBlank()) {
                signingConfigs.getByName("udgivelse")
            } else {
                /* til selvinstallation: samme nøgle som debug, så APK'en kan installeres direkte */
                signingConfigs.getByName("debug")
            }
        }
        getByName("debug") {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
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
        buildConfig = true
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.webkit:webkit:1.11.0")
}
