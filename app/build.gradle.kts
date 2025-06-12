plugins {
    alias(libs.plugins.android.application)
	alias(libs.plugins.kotlin.android)
}

android {
    namespace = "org.qp.android.questopiabundle"
    compileSdk = 35

    defaultConfig {
        applicationId = "org.qp.android.questopiabundle"
        minSdk = 26
        targetSdk = 34
        versionCode = 100000
        versionName = "0.1.0"
		
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
        aidl = true
    }

}

dependencies {
    implementation(project(":libalpha"))
    implementation(project(":libbravo"))
    implementation(project(":libcharlie"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)

    implementation(libs.coroutines)

    implementation(libs.storage)
    implementation(libs.jsoup)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}