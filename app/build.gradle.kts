plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("androidx.navigation.safeargs.kotlin")

    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")

    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.chatapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.chatapp"
        minSdk = 26
        targetSdk = 36
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
    buildFeatures{
        viewBinding = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {


    implementation(platform("com.google.firebase:firebase-bom:34.6.0"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation("androidx.navigation:navigation-fragment-ktx:2.9.6")
    implementation("androidx.navigation:navigation-ui-ktx:2.9.6")

    implementation("com.google.firebase:firebase-analytics")

    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")

    implementation("com.google.dagger:hilt-android:2.56")
    ksp("com.google.dagger:hilt-android-compiler:2.56")

    implementation("com.google.android.material:material:1.13.0")

    implementation("com.github.bumptech.glide:glide:5.0.5")

    implementation("com.google.firebase:firebase-database")

    implementation(libs.firebase.storage)

    implementation("com.airbnb.android:lottie:6.7.1")

    implementation("androidx.datastore:datastore-preferences:1.2.1")

}
apply(plugin = "com.google.gms.google-services")
