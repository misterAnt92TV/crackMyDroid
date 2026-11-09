plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.mpp)
}

val jvmVersion = providers.gradleProperty("build.jvmVersion").map(String::toInt)

kotlin {
    jvm("desktop")
    jvmToolchain(jvmVersion.get())

    sourceSets {
        val desktopMain by getting {
            dependencies {
                implementation(project(":shared"))
                implementation(compose.desktop.currentOs)
                implementation(libs.compose.material3)
                implementation(libs.compose.icons)
                implementation(libs.koin.core)
                implementation(libs.coroutines.swing)
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.crackmydroid.desktop.MainKt"
        jvmArgs += "--enable-native-access=ALL-UNNAMED"
    }
}
