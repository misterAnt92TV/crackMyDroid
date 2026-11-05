import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.mpp)
    alias(libs.plugins.kover)
}

android {
    namespace = "com.crackmydroid.android"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.crackmydroid.android"
        minSdk = 24
        targetSdk = 35
        versionCode = 3
        versionName = "1.3"
        manifestPlaceholders["integrityVerificationEndpoint"] = ""
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources.excludes += listOf("META-INF/LICENSE*", "META-INF/AL2.0", "META-INF/LGPL2.1")
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(project(":shared"))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.leakcanary.android)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.icons)
    implementation(libs.activity.compose)
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    implementation(libs.koin.compose)
    implementation(libs.kermit)
    implementation(libs.rootbeer)
    implementation(libs.material)
    implementation(libs.work.runtime.ktx)
    testImplementation(libs.junit4)
    testImplementation(libs.coroutines.test)
}

kover {
    currentProject {
        createVariant("coverage") {
            add("debug")
        }
    }
}
