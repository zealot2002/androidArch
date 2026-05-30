plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.joy.androidarch"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.joy.androidarch"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
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
}

dependencies {
    implementation(project(":tools"))
    implementation(project(":common"))
    implementation(project(":app_res"))
    implementation(project(":feature-goods"))
    implementation(project(":feature-login"))
    implementation(project(":feature-home"))
    implementation(project(":feature-order"))
    implementation(project(":feature-bill"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.recyclerview)
    implementation(libs.com.google.android.material)
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    implementation(libs.kotlin.stdlib)
}
