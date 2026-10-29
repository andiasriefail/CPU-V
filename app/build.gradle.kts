plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.CPU.V.Vhynlain"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.CPU.V.Vhynlain"
        minSdk = 21
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
        buildConfigField("String", "PRIVACY_POLICY_URL", "\"https://example.com/privacy\"")
        buildConfigField("String", "ANDROID_LOGO_URL_TEMPLATE", "\"https://cdn.jsdelivr.net/npm/simple-icons@13.21.0/icons/android.svg\"")
        buildConfigField("String", "ANDROID_VERSION_LOGO_URL_TEMPLATE", "\"https://commons.wikimedia.org/wiki/Special:FilePath/%s\"")
        buildConfigField("String", "CHIPSET_ICON_URL_TEMPLATE", "\"https://commons.wikimedia.org/wiki/Special:FilePath/%s\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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

    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.viewpager2:viewpager2:1.1.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("com.caverock:androidsvg-aar:1.4")
}
