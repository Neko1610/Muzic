plugins {
    id("com.android.application")
    id("com.google.gms.google-services")

}

android {
    namespace = "com.example.music"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.music"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        multiDexEnabled = true
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}
dependencies {
    // Firebase BOM quản lý version
    implementation(platform("com.google.firebase:firebase-bom:33.1.2"))

    // Firebase modules
    implementation("com.google.firebase:firebase-analytics")       // Analytics
    implementation("com.google.firebase:firebase-auth")            // Authentication
    implementation("com.google.firebase:firebase-firestore")        // Firestore
    implementation("com.google.firebase:firebase-database")         // Realtime Database

    // AndroidX
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.datastore:datastore-preferences:1.1.0") // fix NoClassDefFoundError Firestore
    implementation("androidx.multidex:multidex:2.0.1")               // nếu cần

    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.0")

    // Glide
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // AI / Litert
    implementation("com.google.ai.edge.litert:litert-support-api:1.4.0")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    //Media
    implementation ("androidx.media:media:1.6.0")

    //Transform
    implementation ("jp.wasabeef:glide-transformations:4.3.0")

}
