pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev")
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/dev")
        ivy {
            url = uri("https://cache-redirector.jetbrains.com/download.jetbrains.com/kotlin/native/builds/releases/")
            // Kotlin/Native prebuilt archives are published per host; use macOS x86_64 archive to avoid lookup mismatch
            patternLayout { artifact("[revision]/macos-x86_64/[artifact]-[revision].[ext]") }
            metadataSources { artifact() }
        }
    }
}

rootProject.name = "CrackMyDroid"
include(":androidApp")
include(":shared")
include(":iosApp")
include(":desktopApp")
