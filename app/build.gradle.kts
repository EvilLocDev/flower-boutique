plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.flowerboutique"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.flowerboutique"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String",
            "CLOUDINARY_API_KEY",
            "\"${providers.gradleProperty("CLOUDINARY_API_KEY").get()}\""
        )
        buildConfigField(
            "String",
            "CLOUDINARY_KEY_SECRET",
            "\"${providers.gradleProperty("CLOUDINARY_KEY_SECRET").get()}\""
        )
        buildConfigField(
            "String",
            "CLOUDINARY_NAME",
            "\"${providers.gradleProperty("CLOUDINARY_NAME").get()}\""
        )

    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
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
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(fileTree(mapOf(
        "dir" to "B:\\flower-boutique\\app\\libs",
        "include" to listOf("*.aar", "*.jar"),
        "exclude" to listOf<String>()
    )))
    annotationProcessor(libs.room.compiler)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.common)
    implementation(libs.room.runtime)
    implementation(libs.cloudinary.android)
    implementation(libs.firebase.firestore)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation(platform("com.google.firebase:firebase-bom:34.10.0"))
    implementation(files("libs/zpdk-release-v3.1.aar"))
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    //Dung giao dien dang nhap co san cua firebase
    implementation("com.firebaseui:firebase-ui-auth:9.1.1")

    implementation("com.squareup.picasso:picasso:2.8")
    implementation("com.squareup.okhttp3:okhttp:4.6.0")
    implementation("commons-codec:commons-codec:1.14")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
}
