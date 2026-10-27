# CrackMyDroid — Test ViewModel (`jvmCommonTest`)

> Test dei ViewModel condivisi. Tutti risiedono in `shared/src/jvmCommonTest/` e usano `StandardTestDispatcher` + MockK.

---

## Indice

- [Setup comune](#setup-comune)
- [PenTestingViewModelTest](#pentestingviewmodeltest)
- [PermissionsViewModelTest](#permissionsviewmodeltest)
- [ActivityListViewModelTest](#activitylistviewmodeltest)
- [InstalledAppsViewModelTest](#installedappsviewmodeltest)
- [LogViewModelTest](#logviewmodeltest)
- [TrickViewModelTest](#trickviewmodeltest)

---

## Setup comune

Tutti i ViewModel test seguono lo stesso ciclo di vita:

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class XyzViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp()    { Dispatchers.setMain(dispatcher) }
    @AfterTest  fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `scenario`() = runTest(dispatcher) {
        // 1. Arrange — mockk<UseCase>() + coEvery { }
        // 2. Act     — vm.action(); advanceUntilIdle()
        // 3. Assert  — vm.state.value.field == expected; coVerify { }
        // 4. Cleanup — vm.onCleared()
    }
}
```

**`StandardTestDispatcher`** — orologio virtuale; `advanceUntilIdle()` svuota la coda di coroutine senza aspettare tempo reale.
**`vm.onCleared()`** — cancella il `viewModelScope` del ViewModel; necessario perché nei test JVM non esiste un `ViewModelStore`.

---

## PenTestingViewModelTest

**File:** `shared/src/jvmCommonTest/kotlin/com/crackmydroid/shared/presentation/PenTestingViewModelTest.kt`
**ViewModel:** `PenTestingViewModel`

### Dipendenze iniettate

| Dipendenza | Tipo | Note |
|---|---|---|
| `ListInstalledAppsUseCase` | MockK | |
| `ListPermissionsUseCase` | MockK | |
| `CheckRootUseCase` | MockK | |
| `GetActivitiesUseCase` | MockK | |
| `AnalyzePermissionRisksUseCase` | **reale** | Istanziato direttamente (logica pura) |
| `RunPenTestChecksUseCase` | **reale** | Dipende da `analyzePermissionRisks` |
| `ExportReportUseCase` | reale con port mockato | `ExportReportUseCase(mockk<ReportFileRepository>())` |

---

### Test 1 — `manifestCheckIncludesIntentExposureInPentestResult`

**Obiettivo:** verificare che il check `manifest_flags` rilevi come `PenTestStatus.Error` un'activity con deep-link intent esposto.

**Setup fixture:**
```kotlin
// Un'app con una sola activity con intent action + data URI
val activity = ActivityEntry(
    label              = "DeepLink",
    appLabel           = "Demo App",
    packageName        = "com.demo.app",
    activityName       = "com.demo.app.DeepLinkActivity",
    launchableViaShell = false,
    launchIntentAction = "android.intent.action.VIEW",
    launchIntentData   = "demo://host/details"
)
coEvery { listInstalled() } returns listOf(InstalledAppEntry("Demo App", "com.demo.app", "/data/..."))
coEvery { listPermissions("com.demo.app") } returns AppPermissionEntry("com.demo.app", emptyList())
coEvery { checkRoot() } returns RootStatus(false, "")
coEvery { getActivities("com.demo.app") } returns listOf(activity)
```

**Esecuzione:**
```kotlin
vm.selectApp("com.demo.app", "Demo App")
vm.runSelectedChecks(setOf("manifest_flags"))
advanceUntilIdle()
```

**Assertions:**
```kotlin
val result = vm.state.value.checkResults["manifest_flags"]
assertEquals(PenTestStatus.Error::class, result!!.status::class)
assertTrue(result.error!!.contains("intent/deeplink"))
```

---

### Test 2 — `selectedChecksShareOnePermissionsLoadAndComplete`

**Obiettivo:** verificare che tre check distinti (`network_monitor`, `permission_risk`, `manifest_flags`) condividano una sola chiamata a `listPermissions()` grazie alla lazy cache interna.

**Setup fixture:**
```kotlin
coEvery { listInstalled() } returns listOf(InstalledAppEntry("App", "com.demo.app", "/data/..."))
coEvery { listPermissions("com.demo.app") } returns
    AppPermissionEntry("com.demo.app", listOf(PermissionInfo("android.permission.INTERNET", granted = true)))
coEvery { checkRoot() } returns RootStatus(false, "")
coEvery { getActivities("com.demo.app") } returns emptyList()
```

**Esecuzione:**
```kotlin
vm.selectApp("com.demo.app", "App")
vm.runSelectedChecks(setOf("network_monitor", "permission_risk", "manifest_flags"))
advanceUntilIdle()
```

**Assertions:**
```kotlin
val results = vm.state.value.checkResults
assertEquals(3, results.size)
assertTrue(results.values.none { it.status == PenTestStatus.Queued })  // tutti terminati
coVerify(exactly = 1) { listPermissions("com.demo.app") }               // una sola chiamata
```

---

## PermissionsViewModelTest

**File:** `shared/src/jvmCommonTest/kotlin/com/crackmydroid/shared/presentation/PermissionsViewModelTest.kt`
**ViewModel:** `PermissionsViewModel`

### Dipendenze iniettate

| Dipendenza | Tipo | Note |
|---|---|---|
| `ListPermissionsUseCase` | MockK | |
| `AnalyzePermissionRisksUseCase` | **reale** | |
| `BuildPermissionsReportUseCase` | **reale** | Dipende da `analyzePermissionRisks` |

---

### Test 1 — `setFilterTracksMostFrequentTopQuery`

**Obiettivo:** `topQuery` è la query invocata più volte con `trackUsage=true`.

```kotlin
vm.setFilter("sms",    trackUsage = true)
vm.setFilter("sms",    trackUsage = true)   // 2 volte
vm.setFilter("cam",    trackUsage = true)   // 1 volta
advanceUntilIdle()

assertEquals("sms", vm.state.value.topQuery)
```

---

### Test 2 — `showOnlyRiskyFiltersOutSafeAppsAfterScan`

**Obiettivo:** dopo `startAutoRiskScan()`, il filtro `showOnlyRisky=true` mostra solo le app con permessi pericolosi.

**Setup fixture:**
```kotlin
coEvery { listPermissions("pkg.safe") } returns
    AppPermissionEntry("pkg.safe", listOf(PermissionInfo("android.permission.INTERNET", true)))

coEvery { listPermissions("pkg.risky") } returns
    AppPermissionEntry("pkg.risky", listOf(
        PermissionInfo("android.permission.SYSTEM_ALERT_WINDOW", true)
    ))
```

**Esecuzione:**
```kotlin
vm.startAutoRiskScan()
advanceUntilIdle()

vm.setShowOnlyRisky(true)

val filtered = vm.state.value.filteredApps
assertEquals(1, filtered.size)
assertEquals("pkg.risky", filtered.first().packageName)
```

---

## ActivityListViewModelTest

**File:** `shared/src/jvmCommonTest/kotlin/com/crackmydroid/shared/presentation/ActivityListViewModelTest.kt`
**ViewModel:** `ActivityListViewModel`

### Dipendenze iniettate

| Dipendenza | Tipo | Note |
|---|---|---|
| `GetActivitiesUseCase` | MockK | |
| `LaunchActivityUseCase` | MockK | |
| `GetExportFormatUseCase` | MockK | Ritorna `ExportFormat.TXT` o `ExportFormat.JSON` |

---

### Test 1 — `refreshLoadsActivitiesAndUpdatesState`

```kotlin
coEvery { get() } returns listOf(ActivityEntry("A", "App", "pkg", "Act"))
vm.refresh(); advanceUntilIdle()

assertEquals(1, vm.state.value.activities.size)
coVerify { get() }
```

---

### Test 2 — `setFilterUpdatesImmediatelyAndMatchesActivityName`

```kotlin
// Due attività: "Main" e "Hidden"
coEvery { get() } returns listOf(mainEntry, hiddenEntry)
vm.refresh(); advanceUntilIdle()

vm.setFilter("Hidden")

assertEquals("Hidden", vm.state.value.filter)
assertEquals(1, vm.state.value.groups.size)  // solo il gruppo che contiene "Hidden"
```

---

### Test 3 — `setFilterTracksMostFrequentTopQuery`

```kotlin
vm.setFilter("sec", trackUsage = true)
vm.setFilter("sec", trackUsage = true)   // ×2
vm.setFilter("api", trackUsage = true)   // ×1

assertEquals("sec", vm.state.value.topQuery)
```

---

### Test 4 — `launchWithIntentPassesGuidedArgumentsToUseCase`

**Obiettivo:** `launchWithIntent()` chiama `LaunchActivityUseCase` con esattamente i parametri forniti.

**Fixture:**
```kotlin
val deepLinkEntry = ActivityEntry(
    label              = "DeepLink",
    appLabel           = "Demo App",
    packageName        = "com.demo.app",
    activityName       = "com.demo.app.DeepLinkActivity",
    launchableViaShell = false,
    launchIntentAction = "android.intent.action.VIEW",
    launchIntentData   = "demo://host/details"
)
coEvery { launch(deepLinkEntry, any(), any(), anyOrNull()) } returns Result.success(Unit)
```

**Esecuzione:**
```kotlin
vm.launchWithIntent(deepLinkEntry, "android.intent.action.VIEW", "demo://host/details", null)
advanceUntilIdle()

assertNull(vm.state.value.error)
coVerify { launch(deepLinkEntry, "android.intent.action.VIEW", "demo://host/details", null) }
```

---

### Test 5 — `buildReportAggregatesSelectedAppAnalysis`

**Obiettivo:** `buildReport()` aggrega correttamente conteggi e livelli di rischio per un package.

**Fixture — 4 attività (3 di `com.demo.app`, 1 di altro package):**

```kotlin
val home = ActivityEntry(
    label="Home", packageName="com.demo.app", activityName="...MainActivity",
    launchableViaShell=true
)
val deepLink = ActivityEntry(
    label="DeepLink", packageName="com.demo.app", activityName="...DeepLinkActivity",
    launchableViaShell=false,
    launchIntentAction="android.intent.action.VIEW", launchIntentData="demo://host/details"
)
val hidden = ActivityEntry(
    label="Hidden", packageName="com.demo.app", activityName="...HiddenActivity",
    launchableViaShell=false, launchabilityReason="Resolver non esposto"
)
val other = ActivityEntry(
    label="Other", packageName="com.other.app", activityName="...MainActivity",
    launchableViaShell=true
)
```

**Setup preferiti:**
```kotlin
vm.toggleFavoritePackage("com.demo.app")      // package preferito
vm.toggleFavorite(deepLink)                    // attività preferita
```

**Assertions:**
```kotlin
val report = vm.buildReport("com.demo.app")

assertEquals(3,                          report.totalActivities)
assertEquals(1,                          report.shellLaunchableCount)
assertEquals(1,                          report.guidedLaunchCount)
assertEquals(1,                          report.dumpOnlyCount)
assertEquals(2,                          report.riskyActivitiesCount)
assertEquals(1,                          report.favoriteActivitiesCount)
assertTrue(                              report.packageFavorite)
assertEquals(ActivityIntentRiskLevel.High, report.highestRiskLevel)
```

---

## InstalledAppsViewModelTest

**File:** `shared/src/jvmCommonTest/kotlin/com/crackmydroid/shared/presentation/InstalledAppsViewModelTest.kt`
**ViewModel:** `InstalledAppsViewModel`

### Dipendenze iniettate

| Dipendenza | Tipo | Note |
|---|---|---|
| `ListInstalledAppsUseCase` | MockK | |
| `ExportApkUseCase` | MockK | |
| `ShareApkUseCase` | MockK | |
| `GetExportFormatUseCase` | MockK | |
| `ExportReportUseCase` | reale con port mockato | `ExportReportUseCase(mockk<ReportFileRepository>())` |

**Helper privato:**
```kotlin
private fun reportExporter() = ExportReportUseCase(mockk<ReportFileRepository>())
```

---

### Test 1 — `refreshLoadsInstalledApps`

```kotlin
coEvery { list() } returns listOf(InstalledAppEntry("App", "pkg", "/path"))
vm.refresh(); advanceUntilIdle()

assertEquals(1, vm.state.value.apps.size)
assertNull(vm.state.value.error)
coVerify { list() }
```

---

### Test 2 — `exportStoresPathOnSuccess`

```kotlin
coEvery { export("pkg") } returns Result.success("/tmp/pkg.apk")
vm.export("pkg"); advanceUntilIdle()

assertEquals("/tmp/pkg.apk", vm.state.value.lastExportPath)
assertNull(vm.state.value.error)
coVerify { export("pkg") }
```

---

### Test 3 — `exportAllWritesTheGeneratedReportThroughTheReportPort`

**Obiettivo:** `exportAll()` serializza la lista app nel formato scelto e la scrive tramite `ReportFileRepository`.

```kotlin
val reportFiles = mockk<ReportFileRepository>()
coEvery { format() } returns ExportFormat.JSON
coEvery { reportFiles.save("installed_apps.json", any()) } returns Result.success("/tmp/apps.json")
coEvery { list() } returns listOf(InstalledAppEntry("App", "pkg", "/path"))

vm.refresh(); advanceUntilIdle()
vm.exportAll(); advanceUntilIdle()

assertEquals("/tmp/apps.json", vm.state.value.lastExportPath)
coVerify { reportFiles.save("installed_apps.json", any()) }
```

---

### Test 4 — `shareSetsLastSharedOnSuccess`

```kotlin
coEvery { share("pkg", false) } returns Result.success(Unit)
vm.share("pkg", false); advanceUntilIdle()

assertEquals("pkg", vm.state.value.lastSharedPackage)
assertNull(vm.state.value.error)
```

---

### Test 5 — `shareErrorSetsErrorMessage`

```kotlin
coEvery { share("pkg", true) } returns Result.failure(IllegalStateException("fail"))
vm.share("pkg", true); advanceUntilIdle()

assertNotNull(vm.state.value.error)
```

---

### Test 6 — `filterTracksTopQueryAndFavoritesArePrioritized`

**Obiettivo:** le app preferite appaiono prime nella lista filtrata; `topQuery` rispecchia la query più usata.

```kotlin
// Due app: Alpha (pkg.alpha) e Beta (pkg.beta)
coEvery { list() } returns listOf(
    InstalledAppEntry("Alpha App", "pkg.alpha", "/path/alpha"),
    InstalledAppEntry("Beta App",  "pkg.beta",  "/path/beta")
)
vm.refresh(); advanceUntilIdle()

vm.setFilter("app", trackUsage = true)
vm.setFilter("app", trackUsage = true)   // "app" ×2
vm.setFilter("beta", trackUsage = true)  // "beta" ×1

vm.toggleFavorite("pkg.beta")

assertEquals("app", vm.state.value.topQuery)
assertEquals("pkg.beta", vm.state.value.filteredApps.first().packageName)
```

---

### Test 7 — `setFilterDoesNotTrackTopQueryUntilExplicitSubmit`

```kotlin
vm.setFilter("alpha")                         // senza trackUsage
assertNull(vm.state.value.topQuery)

vm.setFilter("alpha", trackUsage = true)       // con trackUsage
assertEquals("alpha", vm.state.value.topQuery)
```

---

## LogViewModelTest

**File:** `shared/src/jvmCommonTest/kotlin/com/crackmydroid/shared/presentation/LogViewModelTest.kt`
**ViewModel:** `LogViewModel`

### Dipendenze iniettate

| Dipendenza | Tipo |
|---|---|
| `GetLogsUseCase` | MockK |
| `ExportLogsUseCase` | MockK (non stubbed) |

> **Nota:** `tearDown()` usa `Thread.sleep(50)` prima di `resetMain()` per proteggere da race condition nel ViewModel scope.

---

### Test 1 — `setFilterUpdatesImmediatelyAndRecomputesVisibleLogs`

**Obiettivo:** `setFilter()` aggiorna immediatamente `state.filter`.

```kotlin
coEvery { getLogs() } returns listOf(
    "I/Tag(100): init ok",
    "E/Tag(200): fatal issue"
)

vm.refresh(); advanceUntilIdle()
vm.setFilter("fatal")

assertEquals("fatal", vm.state.value.filter)
```

---

## TrickViewModelTest

**File:** `shared/src/jvmCommonTest/kotlin/com/crackmydroid/shared/presentation/TrickViewModelTest.kt`
**ViewModel:** `TrickViewModel`

### Dipendenze iniettate

| Dipendenza | Tipo |
|---|---|
| `ListShellCommandsUseCase` | MockK |
| `ExecuteShellCommandUseCase` | MockK |

---

### Test 1 — `loadPopulatesCommands`

```kotlin
coEvery { listCommands() } returns listOf(
    ShellCommand("Elenco pacchetti", "pm list packages", requiresRoot = false)
)
vm.load(); advanceUntilIdle()

assertEquals(1, vm.state.value.commands.size)
assertEquals("Elenco pacchetti", vm.state.value.commands.first().title)
```

---

### Test 2 — `secondExecuteWhileRunningIsBlockedAndShowsHint`

**Obiettivo:** se un comando è in esecuzione, una seconda chiamata `execute()` viene bloccata con un hint di interazione.

**Setup — simula latenza artificiale:**
```kotlin
val packages = ShellCommand("Elenco pacchetti", "pm list packages", requiresRoot = false)
val top      = ShellCommand("Top processi",     "top -n 1",         requiresRoot = false)

coEvery { listCommands() } returns listOf(packages, top)

// "packages" impiega 120ms (virtuale)
coEvery { execUseCase(packages) } coAnswers {
    delay(120)
    ShellCommandResult(success = true, stdout = "package:…", stderr = "", code = 0)
}
coEvery { execUseCase(top) } returns
    ShellCommandResult(success = true, stdout = "Tasks: …", stderr = "", code = 0)
```

**Esecuzione:**
```kotlin
vm.load(); advanceUntilIdle()

vm.execute(packages)    // inizia, è in corso
vm.execute(top)         // tentativo durante esecuzione → deve essere bloccato

// a metà esecuzione:
assertTrue(vm.state.value.running)
assertTrue(vm.state.value.interactionHint.contains("Elenco pacchetti"))

advanceUntilIdle()      // "packages" completa
```

**Assertions finali:**
```kotlin
assertFalse(vm.state.value.running)
assertEquals("Elenco pacchetti", vm.state.value.lastCommandTitle)
coVerify(exactly = 1) { execUseCase(packages) }
coVerify(exactly = 0) { execUseCase(top) }     // mai eseguito
```
