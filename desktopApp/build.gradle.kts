plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.mpp)
}

kotlin {
    jvm("desktop")
    jvmToolchain(17)

    sourceSets {
        val desktopMain by getting {
            dependencies {
                implementation(project(":shared"))
                implementation(compose.desktop.currentOs)
                implementation(compose.material3)
                implementation(compose.materialIconsExtended)
                implementation(libs.koin.core)
                implementation(libs.coroutines.swing)
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.crackmydroid.desktop.MainKt"
    }
}
