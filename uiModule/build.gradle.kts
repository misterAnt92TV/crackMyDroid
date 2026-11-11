plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.mpp)
    alias(libs.plugins.kover)
}

val androidCompileSdk = providers.gradleProperty("android.compileSdk").map(String::toInt)
val androidMinSdk = providers.gradleProperty("android.minSdk").map(String::toInt)
val jvmVersion = providers.gradleProperty("build.jvmVersion").map(String::toInt)

dependencies {
    debugImplementation(libs.compose.ui.tooling)
}

kotlin {
    jvmToolchain(jvmVersion.get())
    androidTarget()
    jvm("desktop")
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.icons)
            implementation(libs.compose.ui.tooling.preview)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
        androidMain.dependencies {
            implementation(libs.core.ktx)
            implementation(libs.compose.ui)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.activity.compose)
        }
        val desktopMain by getting
        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain.get())
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
        }
        val desktopTest by getting
    }
}

android {
    namespace = "com.crackmydroid.ui"
    compileSdk = androidCompileSdk.get()
    defaultConfig {
        minSdk = androidMinSdk.get()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(jvmVersion.get())
        targetCompatibility = JavaVersion.toVersion(jvmVersion.get())
    }
}

kover {
    currentProject {
        createVariant("coverage") {
            add("debug")
            add("jvm")
        }
    }
}
