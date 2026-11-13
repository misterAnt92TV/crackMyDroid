plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kover)
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
            implementation(libs.coroutines.core)
            implementation(libs.kermit)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.coroutines.test)
            implementation(libs.turbine)
        }
        val jvmCommonTest by creating {
            dependsOn(commonTest.get())
        }
        val androidUnitTest by getting {
            dependsOn(jvmCommonTest)
        }
        val desktopTest by getting {
            dependsOn(jvmCommonTest)
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
    namespace = "com.crackmydroid.architecture"
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
