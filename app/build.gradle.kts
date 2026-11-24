plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.smartview.connect"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.smartview.connect"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }
}
