plugins {
    id("com.android.library")
}

android {
    namespace = "com.joy.featurelogin"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
    }

    buildFeatures {
        viewBinding = true
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
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.kotlin.stdlib)
}