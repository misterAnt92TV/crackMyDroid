package com.crackmydroid.shared.ui.settings.model

/** Public project information shown in Settings; versions remain owned by the Gradle catalog. */
data class ThirdPartyLibrary(
    val name: String,
    val url: String
)

object ThirdPartyLibraries {
    val catalog: List<ThirdPartyLibrary> = listOf(
        ThirdPartyLibrary("Compose Multiplatform", "https://www.jetbrains.com/compose-multiplatform/"),
        ThirdPartyLibrary("Material 3", "https://m3.material.io/"),
        ThirdPartyLibrary("Koin", "https://insert-koin.io/"),
        ThirdPartyLibrary("Kermit", "https://github.com/touchlab/Kermit"),
        ThirdPartyLibrary("Kotlin Coroutines", "https://github.com/Kotlin/kotlinx.coroutines"),
        ThirdPartyLibrary("Kotlinx Serialization", "https://github.com/Kotlin/kotlinx.serialization"),
        ThirdPartyLibrary("SQLDelight", "https://cashapp.github.io/sqldelight/"),
        ThirdPartyLibrary("Ktor Client", "https://ktor.io/"),
        ThirdPartyLibrary("DataStore", "https://developer.android.com/topic/libraries/architecture/datastore"),
        ThirdPartyLibrary("RootBeer", "https://github.com/scottyab/rootbeer"),
        ThirdPartyLibrary("Play Integrity API", "https://developer.android.com/google/play/integrity"),
        ThirdPartyLibrary("AndroidX Core KTX", "https://developer.android.com/kotlin/ktx"),
        ThirdPartyLibrary("AndroidX Activity Compose", "https://developer.android.com/develop/ui/compose/migrate/interoperability-apis/activity-compose"),
        ThirdPartyLibrary("WorkManager", "https://developer.android.com/topic/libraries/architecture/workmanager")
    )
}
