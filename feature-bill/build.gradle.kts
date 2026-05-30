plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.joy.featurebill"
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

ksp {
    arg("AROUTER_MODULE_NAME", project.name)
}

dependencies {
    implementation(project(":tools"))
    implementation(project(":common"))
    implementation(project(":app_res"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.com.google.android.material)
    implementation(libs.kotlin.stdlib)

    ksp(libs.arouter.ksp.compiler)
}
