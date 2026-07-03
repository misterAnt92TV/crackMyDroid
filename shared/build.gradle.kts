import org.jetbrains.compose.ExperimentalComposeLibrary

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.mpp)
    alias(libs.plugins.sqldelight)
}

kotlin {
    jvmToolchain(17)
    androidTarget()
    jvm("desktop")
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.materialIconsExtended)
                implementation(libs.koin.core)
                implementation(libs.kermit)
                implementation(libs.coroutines.core)
                implementation(libs.koin.compose)
                implementation(libs.sqldelight.runtime)
                implementation(libs.sqldelight.coroutines)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.coroutines.test)
            }
        }
        val jvmCommonTest by creating {
            dependsOn(commonTest)
            dependencies {
                implementation(libs.mockk)
                implementation(libs.coroutines.test)
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(libs.core.ktx)
                implementation(libs.compose.ui)
                implementation(libs.compose.material3)
                implementation(libs.compose.ui.tooling.preview)
                implementation(libs.activity.compose)
                implementation(libs.koin.android)
                implementation(libs.koin.androidx.compose)
                implementation(libs.rootbeer)
                implementation(libs.datastore)
                implementation(libs.play.integrity)
                implementation(libs.sqldelight.android)
            }
        }
        val androidUnitTest by getting {
            dependsOn(jvmCommonTest)
            dependencies {
                implementation(libs.mockk)
                implementation(libs.junit4)
                implementation(libs.coroutines.test)
            }
        }
        val desktopMain by getting {
            dependencies {
                implementation(libs.coroutines.swing)
            }
        }
        val desktopTest by getting {
            dependsOn(jvmCommonTest)
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.coroutines.test)
            }
        }
        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
            dependencies {
                implementation(libs.sqldelight.native)
            }
        }
        val iosX64Test by getting
        val iosArm64Test by getting
        val iosSimulatorArm64Test by getting
        val iosTest by creating {
            dependsOn(commonTest)
            iosX64Test.dependsOn(this)
            iosArm64Test.dependsOn(this)
            iosSimulatorArm64Test.dependsOn(this)
        }
    }
}

android {
    namespace = "com.crackmydroid.shared"
    compileSdk = 36
    defaultConfig {
        minSdk = 24
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

sqldelight {
    databases {
        create("CacheDatabase") {
            packageName.set("com.crackmydroid.database")
        }
    }
}
