# CrackMyDroid — Architettura / Architecture

> Documento tecnico di riferimento per l'architettura del progetto.
> Technical reference document for the project architecture.

---

## Indice / Table of Contents

- [Visione d'insieme](#visione-dinsieme--overview)
- [Moduli Gradle](#moduli-gradle--gradle-modules)
- [Shared Module — Layer e Responsabilità](#shared-module--layer-e-responsabilità)
- [Flusso dati Android](#flusso-dati-android--android-data-flow)
- [Flusso dati Desktop](#flusso-dati-desktop--desktop-data-flow)
- [Caching & Paginazione](#caching--paginazione--caching--pagination)
- [Dependency Injection (Koin)](#dependency-injection-koin)
- [PenTesting Engine](#pentesting-engine)
- [Manifest Security Analyzer](#manifest-security-analyzer)
- [Navigazione UI](#navigazione-ui--ui-navigation)
- [Internazionalizzazione](#internazionalizzazione--internationalization)

---

## Visione d'insieme / Overview

```
┌──────────────────────────────────────────────────────────────────────┐
│                         :shared (KMP module)                         │
│                                                                      │
│   ┌─────────────────┐  ┌─────────────────┐  ┌──────────────────┐    │
│   │  Presentation   │  │     Domain      │  │      Data        │    │
│   │                 │  │                 │  │                  │    │
│   │  Screens (10)   │→ │  Use Cases (8)  │→ │  Repositories    │    │
│   │  ViewModels (8) │  │  Models         │  │  (interfaces)    │    │
│   │  BaseViewModel  │  │  Analyzers      │  │                  │    │
│   │  Nav, Components│  │                 │  │                  │    │
│   └─────────────────┘  └─────────────────┘  └──────────────────┘    │
│                                                      │               │
│   commonMain: PaginationHelper · SQLDelight schema   │               │
└──────────────────────────────────────────────────────┼───────────────┘
                                                       │
                          ┌────────────────────────────┤
                          │                            │
               ┌──────────▼──────┐           ┌────────▼──────────┐
               │   androidMain   │           │   desktopMain     │
               │                 │           │                   │
               │ Repository impl │           │ Repository impl   │
               │ (Android APIs,  │           │ (ADB bridge,      │
               │  RootBeer,      │           │  snapshot import, │
               │  Play Integrity,│           │  SQLite JVM)      │
               │  DataStore,     │           │                   │
               │  Caching)       │           └───────────────────┘
               └─────────────────┘
```

---

## Moduli Gradle / Gradle Modules

| Modulo | Tipo | Scopo |
|---|---|---|
| `:shared` | KMP Library | Tutta la logica condivisa: UI, domain, data, DI |
| `:androidApp` | Android Application | Entry point Android; monta la UI condivisa |
| `:desktopApp` | JVM Application | Entry point Desktop; device picker, toolbar ADB |
| `iosApp` | Xcode project | Wrapper SwiftUI |

### Version Catalog (`gradle/libs.versions.toml`)

Le versioni di tutte le dipendenze sono centralizzate nel version catalog. Le principali:

| Libreria | Versione |
|---|---|
| Kotlin | `2.3.21` |
| Compose Multiplatform | `1.10.3` |
| AGP | `8.13.0` |
| Koin | `3.5.6` |
| SQLDelight | `2.0.1` |
| Coroutines | `1.8.1` |
| kotlinx.serialization | `1.6.3` |

---

## Shared Module — Layer e Responsabilità

### Presentation Layer

```
shared/src/commonMain/kotlin/com/crackmydroid/shared/
├── presentation/
│   ├── BaseViewModel.kt          // coroutineScope + lifecycle management
│   ├── UdfViewModel.kt           // Unidirectional Data Flow pattern
│   ├── nav/                      // navigazione Compose
│   ├── components/               // componenti UI riutilizzabili
│   └── operations/               // OperationLogPersistence (expect/actual)
└── ui/
    ├── screens/                  // 10 schermate Compose
    ├── pentest/                  // PenTestingViewModel + PenTestingState
    ├── apps/                     // InstalledAppsViewModel
    ├── activity/                 // ActivityListViewModel
    ├── permissions/              // PermissionsViewModel
    ├── info/                     // InfoViewModel
    ├── log/                      // LogViewModel
    ├── settings/                 // SettingsViewModel
    ├── trick/                    // TrickViewModel
    └── root/                     // RootViewModel
```

**Pattern ViewModel:** tutti estendono `BaseViewModel`. Lo stato è un data class immutabile; le azioni sono sealed class. Il pattern è UDF (Unidirectional Data Flow): `Action → ViewModel → State → UI`.

### Domain Layer

```
domain/
├── model/
│   ├── AppManifestSecurity.kt    // analisi sicurezza manifest
│   ├── ActivityIntentRisk.kt     // rischio esposizione activity
│   ├── PermissionRisk.kt         // classificazione rischio permessi
│   ├── Models.kt                 // AppEntry, AppPermissionEntry, ActivityEntry, RootStatus, …
│   ├── DeviceScanSnapshot.kt     // snapshot completo del device
│   ├── DeviceScanSnapshotContract.kt
│   ├── ExportFormat.kt
│   ├── ResultWrapper.kt          // Success/Failure wrapper
│   └── AppLanguage.kt
└── usecase/
    ├── permissions/              // List, AnalyzeRisk, BuildReport
    ├── activities/               // Get, Analyze
    ├── installedapps/            // List, Export, ShareApk
    ├── root/                     // CheckRoot
    ├── info/                     // GetDeviceInfo
    ├── log/                      // FetchLog, Export
    ├── settings/                 // Get/Set theme, language, prefs
    └── trick/                    // ListCommands, Execute
```

### Data Layer

Le repository sono definite come **interfacce** in `commonMain` e implementate separatamente per ogni piattaforma:

| Repository | androidMain | desktopMain |
|---|---|---|
| `InstalledAppsRepository` | `InstalledAppsRepositoryAndroid` | via ADB/snapshot |
| `ActivityRepository` | `ActivityRepositoryAndroid` | via snapshot |
| `PermissionsRepository` | `PermissionsRepositoryAndroid` | via snapshot |
| `RootCheckRepository` | `RootCheckRepositoryAndroid` (RootBeer) | stub |
| `DeviceInfoRepository` | `DeviceInfoRepositoryAndroid` | via snapshot |
| `ShellCommandRepository` | `ShellCommandRepositoryAndroid` | via `adb shell` |
| `LogRepository` | `LogRepositoryAndroid` | — |
| `SettingsRepository` | `SettingsRepositoryAndroid` (DataStore) | DataStore JVM |

---

## Flusso dati Android / Android Data Flow

```
Package Manager / System APIs
        │
        ▼
[Repository Android] (InstalledApps, Permissions, Activities, …)
        │
        ├──► CachingRepositoryDecorator   (TTL 60s in-memory)
        │
        ├──► SQLDelight CacheDatabase     (SQLite persistente)
        │
        ▼
[Use Case]
        │
        ▼
[ViewModel]  ──► StateFlow<State> ──► [Compose Screen]
```

`AndroidDeviceSnapshotCoordinator` gira in parallelo, serializzando lo stato corrente in JSON per la desktop app.

---

## Flusso dati Desktop / Desktop Data Flow

```
[adb devices]
        │ lista device disponibili
        ▼
DevicePickerScreen
        │ device selezionato
        ▼
DeviceSessionControllerDesktop
        │
        ├──► [adb pull snapshot.json]
        │           │
        │           ▼
        │    RemoteDeviceSnapshotImporter
        │    snapshot.json → DeviceScanSnapshot
        │
        ▼
[Repository Desktop] ← inject snapshot data
        │
        ▼
[Use Case] → [ViewModel] → [Compose Screen]  (identica all'Android)
```

---

## Caching & Paginazione / Caching & Pagination

### PaginationHelper (`commonMain`)

```kotlin
object PaginationHelper {
    fun <T> paginate(items: List<T>, offset: Int, limit: Int): List<T>
    fun countPages(totalItems: Int, pageSize: Int): Int
}
```

Utility stateless per slicing offset-based. Usata da `getActivitiesPaginated()` e `listInstalledPaginated()`.

### CachingRepositoryDecorator (`androidMain`)

```kotlin
class CachingRepositoryDecorator {
    private val cacheValidityMs = 60_000L  // 1 minuto

    suspend fun <T> getCachedOrCompute(
        cache: CacheEntry<T>?,
        compute: suspend () -> T
    ): T

    fun cacheManifestSecurityReport(pkg: String, report: ManifestSecurityReport)
    fun getManifestSecurityCache(pkg: String): ManifestSecurityReport?
    fun invalidateManifestSecurityCache(pkg: String? = null)

    fun cacheComponents(pkg: String, components: List<AppComponent>)
    fun getComponentsCache(pkg: String): List<AppComponent>?
    fun invalidateComponentsCache(pkg: String? = null)
}
```

### CacheMaintenanceRepositoryAndroid

`BroadcastReceiver` che ascolta `ACTION_PACKAGE_REPLACED` / `ACTION_PACKAGE_REMOVED` per invalidare automaticamente le cache quando un'app viene aggiornata o rimossa.

Metodi di invalidazione manuale:
- `clearAllCaches()`
- `invalidateActivitiesCache()`
- `invalidatePermissionsCache()`
- `invalidateInstalledAppsCache()`
- `invalidateManifestSecurityCache()`

---

## Dependency Injection (Koin)

```kotlin
// androidApp
CrackMyDroidApp.onCreate() {
    initKoin {
        androidContext(this@CrackMyDroidApp)
        modules(sharedModule, platformModule)
    }
}
```

I moduli Koin sono:
- `sharedModule` (`commonMain/di/Koin.kt`): ViewModels, Use Cases, repository interfaces
- `platformModule` (`androidMain/di/PlatformModule.kt`): implementazioni Android concrete
- `platformModule` (`desktopMain/di/PlatformModule.kt`): implementazioni Desktop concrete

---

## PenTesting Engine

`PenTestingViewModel` è il cuore della funzionalità di pentest automatico.

### Check disponibili

| checkId | Cosa verifica |
|---|---|
| `permission_risk` | Score rischio permessi + count critical |
| `manifest_flags` | Permessi manifest sensibili + intent risk activity |
| `file_scout` | Root disponibile + permessi storage pericolosi |
| `network_monitor` | INTERNET + permessi dati sensibili (SMS, contatti, posizione…) |
| `lib_scan` | Score rischio elevato + `REQUEST_INSTALL_PACKAGES` |
| *(generic)* | Score critical o activity entry point critici |

### Modalità di esecuzione

- **`SingleTarget`**: scansiona un singolo package
- **`AutoAllApps`**: scansiona tutti i pacchetti installati (con `autoScanJob` cancellabile)

### Limiti history

```kotlin
const val MAX_HISTORY_ITEMS = 240
const val MAX_SAVED_REPORTS = 12
```

I report vengono auto-salvati come snapshot al reset. Export disponibile via `saveTextFile()` / `shareTextFile()`.

---

## Manifest Security Analyzer

```kotlin
object ManifestSecurityAnalyzer {
    fun analyze(security: AppManifestSecurity): ManifestSecurityReport
}
```

### Input: `AppManifestSecurity`

```kotlin
data class AppManifestSecurity(
    val appLabel: String,
    val packageName: String,
    val debuggable: Boolean,
    val allowBackup: Boolean,
    val hasCustomBackupAgent: Boolean,
    val usesCleartextTraffic: Boolean,
    val testOnly: Boolean,
    val targetSdkVersion: Int,
    val minSdkVersion: Int,
    val debugCertSigned: Boolean,
    val exposedComponents: List<ExposedComponentInfo>  // SERVICE, RECEIVER, PROVIDER
)
```

### Output: `ManifestSecurityReport`

```kotlin
data class ManifestSecurityReport(
    val packageName: String,
    val riskLevel: ManifestRiskLevel,      // Low / Medium / High / Critical
    val riskScore: Int,                     // punteggio ponderato
    val findings: List<ManifestSecurityFinding>
)
```

### Pesi dei findings

| Level | Peso |
|---|---|
| Critical | 5 |
| High | 3 |
| Medium | 1 |
| Low | 0 |

---

## Navigazione UI / UI Navigation

```
CrackMyDroidApp (Composable root)
├── BottomBar
│   ├── HomeScreen
│   ├── InfoScreen
│   └── SettingsScreen
└── NavigationDrawer
    ├── InstalledAppsScreen
    ├── PenTestingScreen
    ├── PermissionsScreen
    ├── ActivityListScreen
    ├── RootScreen
    ├── TrickScreen
    └── LogScreen
```

---

## Internazionalizzazione / Internationalization

Le stringhe UI sono definite in `commonMain/i18n/Strings.kt` con supporto per:
- **Italiano** (`AppLanguage.ITALIAN`) — default
- **Inglese** (`AppLanguage.ENGLISH`)

Il rilevamento della lingua di sistema è delegato a `PlatformLocale` (expect/actual):
- `androidMain`: `Locale.getDefault().language`
- `desktopMain`: `System.getProperty("user.language")`

La preferenza utente viene salvata via `SettingsRepository` (DataStore).
