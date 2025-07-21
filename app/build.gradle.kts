plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    kotlin("plugin.serialization") version "2.0.21"
}

android {
    namespace = "com.example.sodappcomposse"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.sodappcomposse"
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    packagingOptions {
        resources {
            pickFirsts.add("META-INF/INDEX.LIST")
            excludes.add("META-INF/DEPENDENCIES")
            pickFirsts.add("META-INF/io.netty.versions.properties")

        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.firebase.appdistribution.gradle)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation("androidx.navigation:navigation-compose:2.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")
    implementation("androidx.compose.material3:material3:1.3.0-alpha03")
    implementation("androidx.compose.material:material-icons-core:1.7.0")

    //para apis

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0") // O la última versión estable

    // Convertidor Gson (para JSON)
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    // O Convertidor Moshi (alternativa popular a Gson)
    // implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    // implementation("com.squareup.moshi:moshi-kotlin:1.15.0") // O la última versión

    // OkHttp (Retrofit lo usa internamente, pero puedes añadirlo explícitamente para configurar el logging interceptor)
    implementation("com.squareup.okhttp3:okhttp:4.12.0") // O la última versión estable
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0") // Para ver logs de las peticiones/respuestas

    // Coroutines (para manejar operaciones asíncronas de forma más sencilla)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3") // O la última versión
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

}