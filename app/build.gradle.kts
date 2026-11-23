plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

/* Versionen kommer fra spillets manifest.json (sendes ind som -PversionName=x.y.z).
   versionCode = a*1.000.000 + b*10.000 + c*100 + d, saa 0.2.43 -> 24300 og 0.2.43.1 -> 24301. */
val spillVersion = (findProperty("versionName") as String?) ?: "0.0.0"
val dele = (0..3).map { spillVersion.split(".").getOrNull(it)?.toIntOrNull() ?: 0 }
val spillKode = dele[0] * 1000000 + dele[1] * 10000 + dele[2] * 100 + dele[3]

android {
    namespace = "dk.allanpersson.hyggespil"
    compileSdk = 36

    defaultConfig {
        applicationId = "dk.allanpersson.hyggespil"
        minSdk = 24
        targetSdk = 36
        versionCode = spillKode
        versionName = spillVersion
        resourceConfigurations += listOf("da", "en")
    }

    signingConfigs {
        create("sideload") {
            val fraMiljoe = System.getenv("ANDROID_KEYSTORE")
            val fil = rootProject.file("sideload/hyggespil.p12")
            if (!fraMiljoe.isNullOrBlank() && File(fraMiljoe).exists()) {
                storeFile = File(fraMiljoe)
                storeType = "PKCS12"
                storePassword = System.getenv("ANDROID_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("ANDROID_KEY_ALIAS")
                keyPassword = System.getenv("ANDROID_KEY_PASSWORD")
                println("signerer med noegle fra miljoeet")
            } else if (fil.exists()) {
                /* Fast noegle til selvinstallation. Ligger i repoet med vilje, saa alle
                   bygninger har samme signatur og telefonen kan opdatere appen.
                   Den maa IKKE bruges til Google Play - se docs/PLAY.md. */
                storeFile = fil
                storeType = "PKCS12"
                storePassword = rootProject.file("sideload/password.txt").readText().trim()
                keyAlias = "hyggespil"
                keyPassword = storePassword
                println("signerer med sideload-noeglen")
            }
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("sideload")
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
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.webkit:webkit:1.12.1")
}
