# CrackMyDroid — Test Desktop e Layer ADB (`desktopTest`)

> Test del layer Desktop: parser ADB, bridge di processo, repository via ADB, session controller, snapshot importer e analisi manifest.

---

## Indice

- [Filosofia: Fakes vs MockK](#filosofia-fakes-vs-mockk)
- [AdbParsersTest](#adbparserstest)
- [ProcessAdbBridgeTest](#processadbbridgetest)
- [AppManifestSecurityTest](#appmanifestsecuritytest)
- [ActivityRepositoryDesktopTest](#activityrepositorydesktoptest)
- [ShellCommandRepositoryDesktopTest](#shellcommandrepositorydesktoptest)
- [RemotePackageManagerDesktopTest](#remotepackagemanagerdesktoptest)
- [DeviceSessionControllerDesktopTest](#devicesessioncontrollerdesktoptest)
- [RemoteDeviceSnapshotImporterTest](#remotedevicesnapshotimportertest)

---

## Filosofia: Fakes vs MockK

I test del layer desktop usano esclusivamente **fakes hand-written** invece di MockK. Motivo: le repository desktop implementano interfacce con comportamenti stateful complessi (cache per sessione, cattura comandi, filesystem), che sono più leggibili e manutenibili come classi concrete che come `mockk<T>()` con `slot` e `answers`.

```kotlin
// Fake tipico nel desktopTest
private class FakeAdbBridge(
    private val commands: Map<String, String> = emptyMap()
) : AdbBridge {
    val executed = mutableListOf<String>()

    override suspend fun shell(command: String, asRoot: Boolean): Result<String> {
        executed += command
        return commands[command]?.let { Result.success(it) }
            ?: Result.failure(RuntimeException("Unknown command: $command"))
    }
    // ... altre impl
}
```

---

## AdbParsersTest

**File:** `shared/src/desktopTest/kotlin/com/crackmydroid/shared/data/adb/AdbParsersTest.kt`
**Soggetto:** `AdbParsers` (funzioni di parsing statiche/pure)
**Pattern:** test puro sincrono — nessun dispatcher, nessun mock

`AdbParsers` trasforma l'output testuale dei comandi ADB in strutture dati Kotlin.

---

### Test 1 — `parsesDeviceList`

**Obiettivo:** `parseAdbDevices()` interpreta correttamente l'output di `adb devices` con stati `device`, `offline`, `unauthorized`.

**Input simulato (`adb devices -l`):**
```
List of devices attached
RZCX20J738K            device product:a54xnsxx model:SM_A546B device:a54x transport_id:1
emulator-5554          offline
192.168.1.100:5555     unauthorized
```

**Assertions:**
```kotlin
assertEquals(3,             devices.size)
assertEquals("RZCX20J738K", devices[0].serial)
assertEquals("SM_A546B",    devices[0].model)
assertEquals("device",      devices[0].state)
assertEquals("offline",     devices[1].state)
assertEquals("unauthorized",devices[2].state)
```

---

### Test 2 — `parsesInstalledAppsAndPmPath`

**Obiettivo:** `parseInstalledApps()` e `parsePmPath()` elaborano correttamente `pm list packages -f`.

**Input simulato:**
```
package:/system/priv-app/Calculator/Calculator.apk=com.android.calculator2
package:/data/app/com.example.demo-1/base.apk=com.example.demo
```

**Assertions:**
```kotlin
// parseInstalledApps
assertEquals(2,                   apps.size)
assertEquals("com.example.demo",  apps[1].packageName)

// parsePmPath
assertTrue(paths.first().endsWith("Calculator.apk"))
```

---

### Test 3 — `parsesPermissionsAndActivitiesFromDump`

**Obiettivo:** `parseRequestedPermissions()` e `parseActivities()` elaborano l'output di `dumpsys package <pkg>`, incluse activity con intent resolver, inner class (`$`), scheme, authority, path e MIME type.

**Input simulato (excerpt da `dumpsys package`):**
```
requested permissions:
  android.permission.CAMERA
  android.permission.INTERNET

Activity Resolver Table:
  Non-Data Actions:
    android.intent.action.VIEW:
      2e91c76 com.example.demo/.MainActivity filter 3e12345
        Action: "android.intent.action.VIEW"
  Schemes:
    demo:
      5f78abc com.example.demo/.Settings$DeepLinkActivity filter ...
        Action: "android.intent.action.VIEW"
        Scheme: "demo"
        Authority: "host.example"
        Path: "/details"
        Type: "image/*"

Activities:
  com.example.demo/.HiddenActivity
```

**Assertions — permessi:**
```kotlin
assertEquals(listOf("CAMERA", "INTERNET"), permissions)  // ordinati
```

**Assertions — attività:**
```kotlin
assertEquals(3, activities.size)

// activities[0] — MainActivity via action
assertEquals("com.example.demo.MainActivity",            activities[0].activityName)
assertEquals(true,                                       activities[0].launchableViaShell)
assertEquals("android.intent.action.VIEW",               activities[0].launchIntentAction)

// activities[1] — DeepLinkActivity via scheme (inner class con $)
assertEquals("com.example.demo.Settings\$DeepLinkActivity", activities[1].activityName)
assertEquals("DeepLinkActivity",                            activities[1].label)
assertEquals(false,                                         activities[1].launchableViaShell)
assertTrue(activities[1].launchabilityReason.contains("intent contestualizzato"))
assertEquals("android.intent.action.VIEW",                  activities[1].launchIntentAction)
assertEquals("demo://host.example/details",                 activities[1].launchIntentData)
assertEquals("image/*",                                     activities[1].launchIntentMimeType)
assertTrue(activities[1].launchContextHint.contains("scheme demo"))
assertTrue(activities[1].launchContextHint.contains("type image/*"))

// activities[2] — HiddenActivity (solo dump)
assertEquals("com.example.demo.HiddenActivity", activities[2].activityName)
assertEquals(false,                              activities[2].launchableViaShell)
```

---

## ProcessAdbBridgeTest

**File:** `shared/src/desktopTest/kotlin/com/crackmydroid/shared/data/adb/ProcessAdbBridgeTest.kt`
**Soggetto:** `ProcessAdbBridge` (implementazione reale — avvia processi di sistema)
**Pattern:** `runTest` con filesystem reale (temp dir)

Questi test verificano la **validazione della configurazione ADB** prima di eseguire qualsiasi processo. Usano `createTempDirectory()` per creare scenari reali.

> Cleanup: ogni test usa `try/finally { dir.deleteRecursively() }` — nessun `@After`.

---

### Test 1 — `rejectsConfiguredDirectoryAsAdbBinary`

**Obiettivo:** se il percorso ADB punta a una **directory** (non un file), il bridge restituisce `Result.failure` con messaggio d'errore.

```kotlin
val dir = createTempDirectory("adb-test-")
try {
    val bridge  = ProcessAdbBridge()
    val result  = bridge.configure(dir.absolutePath.toString())

    assertTrue(result.isFailure)
    assertFalse(result.exceptionOrNull()?.message.isNullOrBlank())
} finally {
    dir.deleteRecursively()
}
```

---

### Test 2 — `rejectsConfiguredNonExecutableFileAsAdbBinary`

**Obiettivo:** se il percorso ADB punta a un file **non eseguibile**, il bridge restituisce `Result.failure`.

```kotlin
val dir  = createTempDirectory("adb-test-")
val file = dir.resolve("fake-adb").toFile().also { it.createNewFile() }
file.setExecutable(false)
try {
    val result = ProcessAdbBridge().configure(file.absolutePath)

    assertTrue(result.isFailure)
    assertFalse(result.exceptionOrNull()?.message.isNullOrBlank())
} finally {
    dir.deleteRecursively()
}
```

---

## AppManifestSecurityTest

**File:** `shared/src/desktopTest/kotlin/com/crackmydroid/shared/domain/model/AppManifestSecurityTest.kt`
**Soggetto:** `ManifestSecurityAnalyzer.analyze()` e `ManifestSecurityAnalyzer.formatLevel()`
**Pattern:** test puro sincrono — nessun mock, nessun dispatcher

Questi test coprono ogni check dell'analizzatore, incluso il calcolo del punteggio composito e le stringhe di localizzazione italiane.

---

### Test 1 — `analyzesDebuggableAppAsCritical`

```kotlin
val security = AppManifestSecurity(
    debuggable          = true,
    allowBackup         = false,
    hasCustomBackupAgent= false,
    usesCleartextTraffic= false,
    testOnly            = false,
    debugCertSigned     = false,
    exposedComponents   = emptyList()
)

val report = ManifestSecurityAnalyzer.analyze(security)

assertEquals(ManifestRiskLevel.Critical,       report.level)
assertTrue(report.findings.any { it.title.contains("debuggable") })
```

---

### Test 2 — `analyzesBackupWithoutAgentAsHigh`

```kotlin
val security = AppManifestSecurity(
    debuggable           = false,
    allowBackup          = true,
    hasCustomBackupAgent = false,   // senza agente → High (non Medium)
    ...
)

val report = ManifestSecurityAnalyzer.analyze(security)

assertEquals(ManifestRiskLevel.High, report.level)
assertTrue(report.findings.any { it.title.contains("Backup") })
```

---

### Test 3 — `analyzesExportedProviderWithoutPermissionAsCritical`

```kotlin
val security = AppManifestSecurity(
    exposedComponents = listOf(
        ExposedComponentInfo(
            name       = "com.example.DataProvider",
            type       = ComponentType.PROVIDER,
            exported   = true,
            permission = null    // ← nessuna permission → Critical
        )
    ), ...
)

val report = ManifestSecurityAnalyzer.analyze(security)

assertEquals(ManifestRiskLevel.Critical, report.level)
assertTrue(report.findings.any { it.title.contains("provider") })
```

---

### Test 4 — `analyzesProtectedExportedComponentAsLowRisk`

```kotlin
val security = AppManifestSecurity(
    exposedComponents = listOf(
        ExposedComponentInfo(
            type       = ComponentType.SERVICE,
            exported   = true,
            permission = "android.permission.INTERNET"  // ← protetto
        )
    ), ...
)

val report = ManifestSecurityAnalyzer.analyze(security)

assertEquals(ManifestRiskLevel.Low, report.level)  // componente protetto = non a rischio
```

---

### Test 5 — `formatsRiskLevelItalian`

```kotlin
assertEquals("CRITICA", ManifestSecurityAnalyzer.formatLevel(ManifestRiskLevel.Critical))
assertEquals("ALTA",    ManifestSecurityAnalyzer.formatLevel(ManifestRiskLevel.High))
assertEquals("MEDIA",   ManifestSecurityAnalyzer.formatLevel(ManifestRiskLevel.Medium))
assertEquals("BASSA",   ManifestSecurityAnalyzer.formatLevel(ManifestRiskLevel.Low))
```

---

### Test 6 — `calculatesCorrectScore`

**Obiettivo:** il punteggio finale è la somma dei pesi di tutti i finding rilevati.

| Flag attivo | Peso |
|---|---|
| `debuggable=true` | 5 |
| `allowBackup=true` (senza agent) | 3 |
| `usesCleartextTraffic=true` | 1 |
| `debugCertSigned=true` | 3 |
| **Totale** | **12** |

```kotlin
val security = AppManifestSecurity(
    debuggable           = true,
    allowBackup          = true,
    hasCustomBackupAgent = false,
    usesCleartextTraffic = true,
    debugCertSigned      = true,
    exposedComponents    = emptyList()
)

val report = ManifestSecurityAnalyzer.analyze(security)

assertEquals(12, report.score)
assertTrue(report.findings.size >= 4)
```

---

## ActivityRepositoryDesktopTest

**File:** `shared/src/desktopTest/kotlin/com/crackmydroid/shared/data/activity/ActivityRepositoryDesktopTest.kt`
**Soggetto:** `ActivityRepositoryDesktop`
**Pattern:** `runTest` con fakes hand-written

### Fakes utilizzati

| Fake | Implementa | Comportamento |
|---|---|---|
| `CapturingActivityAdbBridge` | `AdbBridge` | Cattura l'ultimo comando `shell()` in `var lastCommand`; restituisce sempre `Result.success("Starting: Intent { ... }")` |
| `FakeActivitySessionController` | `DeviceSessionController` | Ritorna stato fisso: `adbPath="/usr/bin/adb"`, device `SERIAL1` |
| `FakeActivityRemotePackageManager` | `RemotePackageManager` | Tutti i metodi ritornano empty |

---

### Test 1 — `guidedLaunchBuildsIntentCommandWithQuotedArguments`

**Obiettivo:** il lancio guidato di una activity con inner class (`$`) costruisce il comando `am start` con tutti gli argomenti singolo-quotati e il `$` correttamente escaped.

**Fixture:**
```kotlin
val entry = ActivityEntry(
    packageName        = "pkg.demo",
    activityName       = "pkg.demo.Settings\$DeepLinkActivity",   // inner class
    launchableViaShell = false,
    launchIntentAction = "android.intent.action.VIEW",
    launchIntentData   = "demo://host/details",
    launchIntentMimeType = "image/*"
)
```

**Esecuzione:**
```kotlin
val repo = ActivityRepositoryDesktop(adbBridge, sessionController, packageManager)
repo.launch(entry.packageName, entry.activityName,
            entry.launchIntentAction, entry.launchIntentData, entry.launchIntentMimeType)
```

**Assertion:**
```kotlin
assertEquals(
    "am start -n 'pkg.demo/pkg.demo.Settings\$DeepLinkActivity'" +
    " -a 'android.intent.action.VIEW'" +
    " -d 'demo://host/details'" +
    " -t 'image/*'",
    adbBridge.lastCommand
)
```

---

## ShellCommandRepositoryDesktopTest

**File:** `shared/src/desktopTest/kotlin/com/crackmydroid/shared/data/shell/ShellCommandRepositoryDesktopTest.kt`
**Soggetto:** `ShellCommandRepositoryDesktop`
**Pattern:** `runTest` con fakes

### Fakes utilizzati

| Fake | Implementa | Comportamento |
|---|---|---|
| `FakeShellAdbBridge` | `AdbBridge` | `which su` → `Result.failure`; `id` (root) → `Result.failure`; altri → `Result.success("")` |
| `FakeShellSessionController` | `DeviceSessionController` | Stato fisso, serial `SERIAL1` |

---

### Test 1 — `marksRootCommandsUnsupportedWhenSuIsUnavailable`

**Obiettivo:** quando `su` non è disponibile sul device, i comandi `requiresRoot=true` vengono marcati `supported=false` con ragione che contiene `"adb-only"`. La chiamata a `execute()` su un comando non supportato ritorna `ShellCommandResult(success=false, code=126)`.

**Esecuzione:**
```kotlin
val commands = repo.listCommands()
val rootCommand = commands.first { it.requiresRoot }
val safeCommand = commands.first { !it.requiresRoot }

// Verifica marcatura
assertFalse(rootCommand.supported)
assertTrue(rootCommand.unsupportedReason.contains("adb-only"))
assertTrue(safeCommand.supported)

// Verifica esecuzione bloccata
val result = repo.execute(rootCommand)
assertFalse(result.success)
assertEquals(126, result.code)
assertEquals(rootCommand.unsupportedReason, result.stderr)
```

---

## RemotePackageManagerDesktopTest

**File:** `shared/src/desktopTest/kotlin/com/crackmydroid/shared/data/packages/RemotePackageManagerDesktopTest.kt`
**Soggetto:** `RemotePackageManagerDesktop`
**Pattern:** `runTest` con fake che registra comandi eseguiti

### Fake utilizzato

```kotlin
private class FakePackageAdbBridge(
    private val responses: Map<String, String>
) : AdbBridge {
    val commands = mutableListOf<String>()   // registra TUTTI i comandi

    override suspend fun shell(command: String, asRoot: Boolean): Result<String> {
        commands += command
        return responses[command]?.let { Result.success(it) }
            ?: Result.failure(RuntimeException("Unknown: $command"))
    }
}
```

**Responses pre-caricate:**
```kotlin
mapOf(
    "pm list packages -f"                    to "<2-package output>",
    "dumpsys package com.example.demo"       to "<permissions dump>",
    "pm path com.example.demo"               to "package:/data/app/com.example.demo/base.apk"
)
```

---

### Test 1 — `cachesPackageQueriesPerSession`

**Obiettivo:** ogni comando ADB viene eseguito **esattamente una volta** per sessione — le chiamate successive usano la cache.

```kotlin
// Chiamate doppie
val firstList  = repo.listInstalledApps()
val secondList = repo.listInstalledApps()   // ← deve usare cache

val firstDump  = repo.readPackageDump("com.example.demo")
val secondDump = repo.readPackageDump("com.example.demo")  // ← cache

val firstPaths  = repo.readPackagePaths("com.example.demo")
val secondPaths = repo.readPackagePaths("com.example.demo") // ← cache

// Uguaglianza dei risultati
assertEquals(firstList,  secondList)
assertEquals(firstDump,  secondDump)
assertEquals(firstPaths, secondPaths)

// ADB invocato solo una volta per comando
assertEquals(1, bridge.commands.count { it == "pm list packages -f" })
assertEquals(1, bridge.commands.count { it == "dumpsys package com.example.demo" })
assertEquals(1, bridge.commands.count { it == "pm path com.example.demo" })
```

---

## DeviceSessionControllerDesktopTest

**File:** `shared/src/desktopTest/kotlin/com/crackmydroid/shared/data/device/DeviceSessionControllerDesktopTest.kt`
**Soggetto:** `DeviceSessionControllerDesktop`
**Pattern:** `runTest` con fakes (inclusa implementazione completa di `SettingsRepository`)

### Fakes utilizzati

| Fake | Implementa | Note |
|---|---|---|
| `FakeAdbBridge` | `AdbBridge` | `listDevices()` ritorna lista iniettata; `shell()` → `""` |
| `FakeSettingsRepository` | `SettingsRepository` | Implementazione in-memory completa con tutti i 20+ campi (theme, language, adbPath, favorites, accessibility…) |

**Interazione con `DesktopScanCache`:** il test usa l'istanza reale di `DesktopScanCache` per verificare l'invalidazione.

---

### Test 1 — `refreshAndSelectDeviceUpdatesStateAndClearsCache`

**Obiettivo:** `selectDevice()` aggiorna lo stato, persiste `adbPath` nelle impostazioni e **invalida** la cache di scan.

**Pre-condizione — cache popolata:**
```kotlin
cache.installedApps = listOf(InstalledAppEntry("Pkg", "pkg", "/tmp/pkg.apk"))
```

**Esecuzione:**
```kotlin
val device = ConnectedDevice(serial = "SERIAL1", state = "device")
controller.refreshDevices("/opt/homebrew/bin/adb")
controller.selectDevice(device, "/opt/homebrew/bin/adb")
advanceUntilIdle()
```

**Assertions:**
```kotlin
val state = controller.state.value

assertEquals("SERIAL1",              state.selectedDevice?.serial)
assertEquals("/opt/homebrew/bin/adb", settings.adbPath)    // persistito
assertNull(cache.installedApps)                              // cache invalidata
assertEquals(1, state.sessionVersion)
```

---

### Test 2 — `rescanAndClearSelectionWork`

**Obiettivo:** `rescanSelectedDevice()` incrementa `sessionVersion` (forza reload); `clearSelection()` deseleziona il device mantenendo la lista disponibile.

```kotlin
controller.refreshDevices("/usr/bin/adb")
controller.selectDevice(device, "/usr/bin/adb")
advanceUntilIdle()

controller.rescanSelectedDevice()
advanceUntilIdle()

assertEquals(2, controller.state.value.sessionVersion)      // incrementato

controller.clearSelection()

assertNull(controller.state.value.selectedDevice)           // deselezionato
assertTrue(controller.state.value.availableDevices.isNotEmpty())  // lista mantenuta
```

---

## RemoteDeviceSnapshotImporterTest

**File:** `shared/src/desktopTest/kotlin/com/crackmydroid/shared/data/snapshot/RemoteDeviceSnapshotImporterTest.kt`
**Soggetto:** `RemoteDeviceSnapshotImporter`
**Pattern:** `runTest` con fake che simula `adb pull` scrivendo su filesystem reale

### Fake utilizzato

```kotlin
private class FakeSnapshotAdbBridge(
    var snapshotText: String?
) : AdbBridge {
    override suspend fun pull(remotePath: String, localPath: String): Result<Unit> {
        return snapshotText?.let { text ->
            File(localPath).also { it.parentFile?.mkdirs() }.writeText(text)
            Result.success(Unit)
        } ?: Result.failure(RuntimeException("Snapshot not available"))
    }
    override suspend fun shell(command: String, asRoot: Boolean) = Result.success("")
}
```

Il fake simula `adb pull` scrivendo il testo dello snapshot su un file locale — lo stesso comportamento del bridge reale.

---

### Test 1 — `importsValidSnapshotIntoCache`

**Obiettivo:** uno snapshot valido viene scaricato via ADB, deserializzato e distribuito correttamente in tutti i campi di `DesktopScanCache`.

**Input — snapshot costruito programmaticamente:**
```kotlin
val snapshot = DeviceScanSnapshot(
    schemaVersion    = DeviceScanSnapshot.SCHEMA_VERSION,
    deviceSerial     = "SERIAL1",
    deviceInfo       = SectionData(READY, data = DeviceInfo(model = "Pixel 7", ...)),
    rootStatus       = SectionData(READY, data = RootStatus(isRooted = false, details = "No root")),
    playIntegrity    = SectionData(SKIPPED),
    installedApps    = SectionData(READY, data = listOf(InstalledAppEntry("Demo", "pkg.demo", "/path"))),
    logs             = SectionData(READY, data = listOf("I/Test: ok")),
    ...
)
bridge.snapshotText = DeviceScanSnapshotJson.format.encodeToString(snapshot)
```

**Assertions:**
```kotlin
val outcome = importer.importSnapshot("SERIAL1")

assertTrue(outcome.usedSnapshot)
assertEquals("1.2", outcome.metadata.sourceAppVersion)

// Cache popolata correttamente
assertNotNull(cache.deviceInfo)
assertEquals("pkg.demo",  cache.installedApps!!.first().packageName)
assertEquals("No root",   cache.rootStatus!!.details)
assertEquals("I/Test: ok",cache.logs!!.first())

// Sezione SKIPPED → fallback appropriato
assertEquals("Non eseguito automaticamente", cache.playIntegrity!!.details)
```

---

### Test 2 — `fallsBackWhenSnapshotSchemaIsIncompatible`

**Obiettivo:** uno snapshot con `schemaVersion` futura/incompatibile viene rifiutato; la cache rimane vuota e il messaggio di fallback contiene `"schema"`.

**Input:**
```json
{
  "schemaVersion": 999,
  "deviceSerial": "SERIAL1",
  ...
}
```

**Assertions:**
```kotlin
val outcome = importer.importSnapshot("SERIAL1")

assertFalse(outcome.usedSnapshot)
assertTrue(outcome.message.contains("schema"))
assertNull(cache.deviceInfo)   // cache non toccata
```
