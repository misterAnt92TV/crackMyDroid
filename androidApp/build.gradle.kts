import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.mpp)
    alias(libs.plugins.kover)
}

val androidCompileSdk = providers.gradleProperty("android.compileSdk").map(String::toInt)
val androidTargetSdk = providers.gradleProperty("android.targetSdk").map(String::toInt)
val androidMinSdk = providers.gradleProperty("android.minSdk").map(String::toInt)
val jvmVersion = providers.gradleProperty("build.jvmVersion").map(String::toInt)

android {
    namespace = "com.crackmydroid.android"
    compileSdk = androidCompileSdk.get()

    defaultConfig {
        applicationId = "com.crackmydroid.android"
        minSdk = androidMinSdk.get()
        targetSdk = androidTargetSdk.get()
        versionCode = 4
        versionName = "1.4"
        manifestPlaceholders["integrityVerificationEndpoint"] = ""
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(jvmVersion.get())
        targetCompatibility = JavaVersion.toVersion(jvmVersion.get())
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
        jvmTarget.set(jvmVersion.map { JvmTarget.fromTarget(it.toString()) })
    }
}

dependencies {
    implementation(project(":architectureModule"))
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
