plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
}

val androidCompileSdk = providers.gradleProperty("android.compileSdk").map(String::toInt)
val androidMinSdk = providers.gradleProperty("android.minSdk").map(String::toInt)
val jvmVersion = providers.gradleProperty("build.jvmVersion").map(String::toInt)

kotlin {
    jvmToolchain(jvmVersion.get())
    androidTarget()
    jvm("desktop")
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            api(project(":architectureModule"))
            api(libs.coroutines.test)
            api(libs.turbine)
            api(kotlin("test"))
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain.get())
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
        }
        val iosX64Test by getting
        val iosArm64Test by getting
        val iosSimulatorArm64Test by getting
        val iosTest by creating {
            dependsOn(commonTest.get())
            iosX64Test.dependsOn(this)
            iosArm64Test.dependsOn(this)
            iosSimulatorArm64Test.dependsOn(this)
        }
    }
}

android {
    namespace = "com.crackmydroid.architecture.testing"
    compileSdk = androidCompileSdk.get()
    defaultConfig {
        minSdk = androidMinSdk.get()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(jvmVersion.get())
        targetCompatibility = JavaVersion.toVersion(jvmVersion.get())
    }
}
