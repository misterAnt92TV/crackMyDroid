# CrackMyDroid — Test Use Case, Modelli e Componenti (`jvmCommonTest`)

> Test del layer domain e dei componenti condivisi: use case, serializzazione snapshot,
> `SearchHistoryStore`.

---

## Indice

- [Copertura diretta degli UseCase](#copertura-diretta-degli-usecase)
- [UseCaseTests](#usecasetests)
- [DeviceScanSnapshotJsonTest](#devicescansnapshottjsontest)
- [SearchHistoryStoreTest](#searchhistorystoretest)

---

## Copertura diretta degli UseCase

I **45 UseCase** in `shared/src/commonMain/kotlin/com/crackmydroid/shared/domain/usecase/`
sono esercitati direttamente dai test in `shared/src/jvmCommonTest/kotlin/com/crackmydroid/shared/domain/usecase/`.
La matrice indica test che costruiscono ed eseguono il soggetto reale: i mock di UseCase nei test
ViewModel non sono considerati copertura diretta.

| UseCase | Suite diretta | Comportamenti verificati |
|---------|---------------|--------------------------|
| `GetActivitiesUseCase` | `UseCaseDelegationTest` | Lista completa/vuota, errore repository |
| `LaunchActivityUseCase` | `UseCaseDelegationTest` | Parametri intent espliciti/default, successo, Result.failure, eccezione |
| `GetDeviceInfoUseCase` | `UseCaseDelegationTest` | Modello restituito con campi opzionali assenti, errore |
| `ExportApkUseCase` | `UseCaseTests`, `UseCaseDelegationTest` | Successo, fallimento ed eccezione |
| `ListInstalledAppsUseCase` | `UseCaseDelegationTest` | Lista completa/vuota, errore |
| `ShareApkUseCase` | `UseCaseDelegationTest` | Entrambi i valori bluetoothOnly, successo, fallimento ed eccezione |
| `ExportLogsUseCase` | `log/ExportLogsUseCaseTest`, `UseCaseDelegationTest` | Formati TXT/JSON/PDF, contenuto e nome file, percorso, fallimento ed eccezione |
| `GetLogsUseCase` | `UseCaseDelegationTest` | Lista completa/vuota, ordine e duplicati, errore |
| `RunPenTestChecksUseCase` | `pentest/RunPenTestChecksUseCaseTest` | Success/finding per ogni check, dati assenti, soglie library e fallback |
| `AnalyzePermissionRisksUseCase` | `permissions/AnalyzePermissionRisksUseCaseTest` | Famiglie e pattern, normalizzazione, duplicati, score, conteggi, etichette e ordinamento |
| `BuildPermissionsReportUseCase` | `permissions/BuildPermissionsReportUseCaseTest` | Report vuoto/singolo/aggregato, totali, ordinamento, filename |
| `ListPermissionsUseCase` | `UseCaseDelegationTest` | Lista completa/vuota, errore |
| `ExportReportUseCase` | `reports/ExportReportUseCaseTest` | Save/share, percorso e ordine, Result.failure/eccezioni, nessuna share dopo save fallita |
| `CheckPlayIntegrityUseCase` | `UseCaseDelegationTest` | Nonce default/esplicito, esito disponibile/non disponibile, errore |
| `CheckRootUseCase` | `UseCaseDelegationTest` | Modalità PASSIVE default e PRIVILEGE_ACCESS, root presente/assente, errore |
| `ExecuteShellCommandUseCase` | `UseCaseDelegationTest` | Comando e flag root, output successo/fallimento, eccezione |
| `ListShellCommandsUseCase` | `UseCaseDelegationTest` | Lista completa/vuota, errore |
| `GetActivityFavoritesUseCase` | `SettingsUseCasesTest` | Set vuoto/popolato, errore lettura |
| `SetActivityFavoritesUseCase` | `SettingsUseCasesTest` | Valore scritto vuoto/popolato, errore scrittura |
| `GetActivityPackageFavoritesUseCase` | `SettingsUseCasesTest` | Set vuoto/popolato, errore lettura |
| `SetActivityPackageFavoritesUseCase` | `SettingsUseCasesTest` | Valore scritto vuoto/popolato, errore scrittura |
| `GetExportFormatUseCase` | `SettingsUseCasesTest` | Tutti i formati, errore lettura |
| `SetExportFormatUseCase` | `SettingsUseCasesTest` | Ogni formato scritto, errore scrittura |
| `GetFeatureHintsEnabledUseCase` | `SettingsUseCasesTest` | True/false, errore lettura |
| `SetFeatureHintsEnabledUseCase` | `SettingsUseCasesTest` | True/false scritti, errore scrittura |
| `GetHighContrastUseCase` | `SettingsUseCasesTest` | True/false, errore lettura |
| `SetHighContrastUseCase` | `SettingsUseCasesTest` | True/false scritti, errore scrittura |
| `GetInstalledAppFavoritesUseCase` | `SettingsUseCasesTest` | Set vuoto/popolato, errore lettura |
| `SetInstalledAppFavoritesUseCase` | `SettingsUseCasesTest` | Valore scritto vuoto/popolato, errore scrittura |
| `GetIntroSeenUseCase` | `SettingsUseCasesTest` | True/false, errore lettura |
| `SetIntroSeenUseCase` | `SettingsUseCasesTest` | True/false scritti, errore scrittura |
| `GetLanguageUseCase` | `SettingsUseCasesTest` | Tutte le lingue incluso SYSTEM, errore lettura |
| `SetLanguageUseCase` | `SettingsUseCasesTest` | Ogni lingua scritta incluso SYSTEM, errore scrittura |
| `GetLargeTextUseCase` | `SettingsUseCasesTest` | True/false, errore lettura |
| `SetLargeTextUseCase` | `SettingsUseCasesTest` | True/false scritti, errore scrittura |
| `GetPermissionFavoritesUseCase` | `SettingsUseCasesTest` | Set vuoto/popolato, errore lettura |
| `SetPermissionFavoritesUseCase` | `SettingsUseCasesTest` | Valore scritto vuoto/popolato, errore scrittura |
| `GetReduceMotionUseCase` | `SettingsUseCasesTest` | True/false, errore lettura |
| `SetReduceMotionUseCase` | `SettingsUseCasesTest` | True/false scritti, errore scrittura |
| `GetSuggestionsEnabledUseCase` | `SettingsUseCasesTest` | True/false, errore lettura |
| `SetSuggestionsEnabledUseCase` | `SettingsUseCasesTest` | True/false scritti, errore scrittura |
| `GetThemeUseCase` | `SettingsUseCasesTest` | Tutti i temi incluso SYSTEM, errore lettura |
| `SetThemeUseCase` | `SettingsUseCasesTest` | Ogni tema scritto incluso SYSTEM, errore scrittura |
| `GetVerboseLogUseCase` | `SettingsUseCasesTest` | True/false, errore lettura |
| `SetVerboseLogUseCase` | `SettingsUseCasesTest` | True/false scritti, errore scrittura |

### Esecuzione mirata

`jvmCommonTest` è un source set condiviso, **non un task Gradle eseguibile**.
Le stesse suite vengono eseguite dai due target JVM:

```bash
./gradlew :shared:desktopTest --tests 'com.crackmydroid.shared.domain.usecase.*'
./gradlew :shared:testDebugUnitTest --tests 'com.crackmydroid.shared.domain.usecase.*'
```

Le due esecuzioni verificate comprendono ciascuna **54 metodi di test in 7 classi**;
diversi metodi verificano più valori o più UseCase. Questo inventario non equivale a una
percentuale di line/branch coverage. I test JVM non attestano l'esecuzione su Kotlin/Native iOS.

---

## UseCaseTests

**File:** `shared/src/jvmCommonTest/kotlin/com/crackmydroid/shared/domain/usecase/UseCaseTests.kt`
**Package:** `com.crackmydroid.shared.domain.usecase`
**Pattern:** `runTest` con `coEvery` / `coVerify` (MockK)

Questa suite verifica alcune deleghe alla repository sottostante e il flusso save/share.
Per la copertura di tutti gli UseCase e dei casi di errore, consultare la matrice sopra.

---

### Test 1 — `getActivitiesDelegatesToRepo`

```kotlin
val repo = mockk<ActivityRepository>()
coEvery { repo.getActivities() } returns listOf(ActivityEntry("A", "App", "pkg", "Act"))

val useCase = GetActivitiesUseCase(repo)
val result = useCase()

assertEquals(1, result.size)
coVerify { repo.getActivities() }
```

---

### Test 2 — `checkRootReturnsRepoValue`

```kotlin
val repo = mockk<RootCheckRepository>()
coEvery { repo.checkRoot() } returns RootStatus(isRooted = true, details = "root")

val result = CheckRootUseCase(repo)()

assertTrue(result.isRooted)
coVerify { repo.checkRoot() }
```

---

### Test 3 — `listInstalledAppsDelegatesToRepo`

```kotlin
val repo = mockk<InstalledAppsRepository>()
coEvery { repo.listInstalled() } returns listOf(InstalledAppEntry("App", "pkg", "/path"))

val result = ListInstalledAppsUseCase(repo)()

assertEquals(1, result.size)
coVerify { repo.listInstalled() }
```

---

### Test 4 — `exportApkDelegatesToRepo`

```kotlin
val repo = mockk<InstalledAppsRepository>()
coEvery { repo.exportApk("pkg") } returns Result.success("/tmp/pkg.apk")

val result = ExportApkUseCase(repo)("pkg")

assertTrue(result.isSuccess)
coVerify { repo.exportApk("pkg") }
```

---

### Test 5 — `shareApkDelegatesToRepo`

```kotlin
val repo = mockk<InstalledAppsRepository>()
coEvery { repo.shareApk("pkg", true) } returns Result.success(Unit)

val result = ShareApkUseCase(repo)("pkg", useBluetooth = true)

assertTrue(result.isSuccess)
coVerify { repo.shareApk("pkg", true) }
```

---

## DeviceScanSnapshotJsonTest

**File:**
`shared/src/jvmCommonTest/kotlin/com/crackmydroid/shared/domain/model/DeviceScanSnapshotJsonTest.kt`
**Package:** `com.crackmydroid.shared.domain.model`
**Pattern:** unit test puro — nessun mock, nessun dispatcher **Soggetto:**
`DeviceScanSnapshotJson.format` (oggetto `kotlinx.serialization` JSON)

Questi test verificano che il modello `DeviceScanSnapshot` — usato come contratto di serializzazione
tra app Android e desktop — sopravviva intatto al ciclo encode → decode.

---

### Struttura di `DeviceScanSnapshot`

```kotlin
data class DeviceScanSnapshot(
    val schemaVersion : Int,
    val deviceSerial  : String,
    val createdAtEpochMs : Long,
    val deviceInfo    : SectionData<DeviceInfo>,
    val rootStatus    : SectionData<RootStatus>,
    val playIntegrity : SectionData<PlayIntegrityResult>,
    val installedApps : SectionData<List<InstalledAppEntry>>,
    val permissions   : SectionData<List<AppPermissionEntry>>,
    val activities    : SectionData<List<ActivityEntry>>,
    val logs          : SectionData<List<String>>
)

// Ogni sezione può essere in uno di tre stati:
enum class SectionState { READY, FAILED, SKIPPED }

data class SectionData<T>(
    val state : SectionState,
    val data  : T? = null,       // presente solo se READY
    val error : String? = null   // presente solo se FAILED
)
```

---

### Test 1 — `roundTripSnapshotWithReadyFailedAndSkippedSections`

**Obiettivo:** tutti e tre gli stati di `SectionData` (`READY`, `FAILED`, `SKIPPED`) sopravvivono al
round-trip JSON.

**Input:**

```kotlin
val snapshot = DeviceScanSnapshot(
    schemaVersion    = DeviceScanSnapshot.SCHEMA_VERSION,
    deviceSerial     = "SERIAL1",
    createdAtEpochMs = 1_000L,
    deviceInfo       = SectionData(READY, data = DeviceInfo(model = "Pixel 7", ...)),
    rootStatus       = SectionData(READY, data = RootStatus(isRooted = false, details = "")),
    playIntegrity    = SectionData(SKIPPED),                 // ← skipped
    installedApps    = SectionData(READY, data = listOf(...)),
    permissions      = SectionData(FAILED, error = "Permessi non disponibili"),  // ← failed
    activities       = SectionData(READY, data = listOf(...)),
    logs             = SectionData(READY, data = listOf("I/Test: ok"))
)
```

**Esecuzione:**

```kotlin
val json    = DeviceScanSnapshotJson.format.encodeToString(snapshot)
val decoded = DeviceScanSnapshotJson.format.decodeFromString<DeviceScanSnapshot>(json)
```

**Assertions:**

```kotlin
assertEquals(DeviceScanSnapshot.SCHEMA_VERSION, decoded.schemaVersion)
assertEquals("SERIAL1",                          decoded.deviceSerial)
assertEquals(SectionState.FAILED,                decoded.permissions.state)
assertEquals("Permessi non disponibili",         decoded.permissions.error)
assertEquals(SectionState.SKIPPED,               decoded.playIntegrity.state)
assertNull(decoded.playIntegrity.data)
assertEquals("I/Test: ok",                       decoded.logs.data?.first())
```

---

### Test 2 — `ignoresUnknownFieldsWhenDecodingSnapshot`

**Obiettivo:** un JSON con campi sconosciuti non causa eccezioni (`ignoreUnknownKeys = true`).

**Input (JSON raw):**

```json
{
  "schemaVersion": 1,
  "deviceSerial": "SERIAL1",
  "createdAtEpochMs": 10,
  "unknownField": "questo campo verrà ignorato",
  "rootStatus": { "state": "failed", "error": "Root check failed" },
  "deviceInfo": { "state": "ready", "data": { "model": "Pixel 7" } },
  ...
}
```

**Assertions:**

```kotlin
assertEquals(10L,                decoded.createdAtEpochMs)
assertEquals(SectionState.FAILED, decoded.rootStatus.state)
assertNotNull(decoded.deviceInfo.data)
// Nessuna eccezione sollevata
```

---

## SearchHistoryStoreTest

**File:**
`shared/src/jvmCommonTest/kotlin/com/crackmydroid/shared/presentation/components/SearchHistoryStoreTest.kt`
**Package:** `com.crackmydroid.shared.presentation.components`
**Pattern:** unit test puro sincrono **Soggetto:** `SearchHistoryStore` (singleton in-memory)

`SearchHistoryStore` mantiene la cronologia delle ricerche per ogni `SearchFieldWithHistory`
nell'app. È un singleton con stato globale — i test devono resettarlo esplicitamente prima e dopo
ogni esecuzione.

### Configurazione `SearchHistoryStore`

| Proprietà                             | Valore default                                 |
|---------------------------------------|------------------------------------------------|
| Max entries globali per chiave        | 8                                              |
| Max entries visibili nei suggerimenti | 6                                              |
| Ordine suggerimenti                   | più recente prima + prefix match prioritizzato |
| Exact match escluso                   | sì (non suggerire la query corrente)           |

### Setup e teardown

```kotlin
@BeforeTest fun setUp() {
    SearchHistoryStore.setEnabled(true)
    SearchHistoryStore.clearAll()
}

@AfterTest fun tearDown() {
    SearchHistoryStore.clearAll()
    SearchHistoryStore.setEnabled(true)
}
```

---

### Test 1 — `addNormalizesAndDeduplicatesCaseInsensitive`

**Obiettivo:** la stessa query (indipendentemente da maiuscole e spazi) è deduplicata; la versione
più recente vince.

```kotlin
SearchHistoryStore.add("apps", "  Demo   Query  ")  // normalizzato → "demo query"
SearchHistoryStore.add("apps", "demo query")         // stesso valore → deduplicato
SearchHistoryStore.add("apps", "Second Query")

val suggestions = SearchHistoryStore.suggestions("apps")

assertEquals(listOf("Second Query", "demo query"), suggestions)
// "demo query" appare una sola volta, "Second Query" è più recente → prima
```

---

### Test 2 — `suggestionsPrioritizePrefixAndHideExactMatch`

**Obiettivo:** i suggerimenti: (a) escludono la query corrente esatta, (b) mostrano max 6 risultati,
(c) mettono prime le entry con prefix match.

```kotlin
// Aggiungi 8 entry: "log", "logcat", "log level", "login", "root", "root check",
//                   "root beer", "network"
listOf("log","logcat","log level","login","root","root check","root beer","network")
    .forEach { SearchHistoryStore.add("logs", it) }

val suggestions = SearchHistoryStore.suggestions("logs", currentQuery = "log")

assertTrue(suggestions.size <= 6)
assertFalse(suggestions.contains("log"))        // exact match escluso
assertTrue(suggestions.first().startsWith("log"))  // prefix match prima
```

---

### Test 3 — `disabledSuggestionsDoNotStoreOrShowHistory`

**Obiettivo:** `setEnabled(false)` disabilita completamente la funzionalità — né le entry vengono
salvate né i suggerimenti restituiti.

```kotlin
SearchHistoryStore.setEnabled(false)
SearchHistoryStore.add("permissions", "camera")

val suggestions = SearchHistoryStore.suggestions("permissions")

assertTrue(suggestions.isEmpty())
```
