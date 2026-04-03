plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.finalsenior"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.finalsenior"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

    implementation ("io.github.mayzs:paho.mqtt.android:1.2.1")
    implementation("com.android.volley:volley:1.2.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.3")
    implementation("androidx.activity:activity:1.10.1")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("androidx.localbroadcastmanager:localbroadcastmanager:1.1.0")
    implementation("org.videolan.android:libvlc-all:3.5.1")



    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.preference)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

}