plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.mpp)
    alias(libs.plugins.sqldelight)
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
            api(project(":architectureModule"))
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.icons)
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.koin.core)
            implementation(libs.kermit)
            implementation(libs.coroutines.core)
            implementation(libs.koin.compose)
            implementation(libs.sqldelight.runtime)
            implementation(libs.sqldelight.coroutines)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.logging)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.coroutines.test)
            implementation(libs.turbine)
            implementation(libs.ktor.client.mock)
        }
        val jvmCommonTest by creating {
            dependsOn(commonTest.get())
            dependencies {
                implementation(libs.mockk)
                implementation(libs.sqldelight.sqlite)
            }
        }
        androidMain.dependencies {
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
            implementation(libs.ktor.client.okhttp)
        }
        val androidUnitTest by getting {
            dependsOn(jvmCommonTest)
            dependencies {
                implementation(libs.junit4)
            }
        }
        val desktopMain by getting {
            dependencies {
                implementation(libs.coroutines.swing)
                implementation(libs.ktor.client.cio)
                implementation(libs.pdfbox)
            }
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
            dependencies {
                implementation(libs.sqldelight.native)
                implementation(libs.ktor.client.darwin)
            }
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
    namespace = "com.crackmydroid.shared"
    compileSdk = androidCompileSdk.get()
    defaultConfig {
        minSdk = androidMinSdk.get()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(jvmVersion.get())
        targetCompatibility = JavaVersion.toVersion(jvmVersion.get())
    }
}

sqldelight {
    databases {
        create("CacheDatabase") {
            packageName.set("com.crackmydroid.database")
        }
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
