# CrackMyDroid — Documentazione API / API Documentation

> Riferimento per i modelli di dominio, le repository e i use case esposti dal modulo `:shared`.
> Reference for domain models, repositories, and use cases exposed by the `:shared` module.

---

## Indice / Table of Contents

- [Domain Models](#domain-models)
- [Repository Interfaces](#repository-interfaces)
- [Use Cases](#use-cases)
- [ViewModels & State](#viewmodels--state)
- [Caching API](#caching-api)
- [Utilities](#utilities)

---

## Domain Models

### `InstalledAppEntry` provenance

The installed-app inventory includes an `InstallSourceInfo` value. `GOOGLE_PLAY` means the OS
reported `com.android.vending` as installer; it is not a signature check, Play license, or malware
assessment. Android provides richer values on API 30+, while Desktop ADB and older Android may
return `UNAVAILABLE`.

### `PlayIntegrityResult`

The integrity result applies only to CrackMyDroid and the device requesting its token. A decoded
backend verdict can contain `appRecognitionVerdict`, `appLicensingVerdict`, device-recognition
labels and an optional Play Protect verdict.
See [the backend contract](play-integrity-backend-contract.md).

### `AppEntry`

Rappresenta un'app installata sul device.

```kotlin
data class AppEntry(
    val packageName: String,
    val label: String,
    val versionName: String,
    val versionCode: Long,
    val isSystemApp: Boolean,
    val installedAt: Long,        // epoch ms
    val updatedAt: Long,          // epoch ms
    val targetSdkVersion: Int,
    val minSdkVersion: Int
)
```

---

### `AppPermissionEntry`

Permessi dichiarati e concessi per un pacchetto.

```kotlin
data class AppPermissionEntry(
    val packageName: String,
    val appLabel: String,
    val permissions: List<PermissionInfo>
)

data class PermissionInfo(
    val name: String,             // es. android.permission.CAMERA
    val granted: Boolean,
    val protectionLevel: Int      // NORMAL=0, DANGEROUS=1, SIGNATURE=2, …
)
```

---

### `PermissionRisk`

Classificazione del rischio per un permesso.

```kotlin
enum class PermissionRiskLevel { LOW, MEDIUM, HIGH, CRITICAL }

data class PermissionRiskEntry(
    val permissionName: String,
    val riskLevel: PermissionRiskLevel,
    val rationale: String         // spiegazione del rischio
)

data class PermissionRiskReport(
    val packageName: String,
    val riskScore: Int,
    val criticalCount: Int,
    val highCount: Int,
    val entries: List<PermissionRiskEntry>
)
```

---

### `ActivityEntry`

Un'activity esportata o rilevata nel package.

```kotlin
data class ActivityEntry(
    val packageName: String,
    val activityName: String,
    val exported: Boolean,
    val permission: String?,      // permission richiesta per il lancio
    val intentFilters: List<IntentFilterInfo>
)
```

---

### `ActivityIntentRisk` / `ActivityIntentRiskReport`

Analisi del rischio di esposizione di un'activity tramite intent.

```kotlin
enum class ActivityRiskLevel { NONE, LOW, MEDIUM, HIGH, CRITICAL }

data class ActivityIntentRiskReport(
    val activity: ActivityEntry,
    val riskLevel: ActivityRiskLevel,
    val findings: List<String>
)
```

---

### `AppManifestSecurity`

Dati di sicurezza estratti dal manifest APK.

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
    val exposedComponents: List<ExposedComponentInfo>
)

data class ExposedComponentInfo(
    val name: String,
    val type: ComponentType,      // SERVICE, RECEIVER, PROVIDER
    val exported: Boolean,
    val permission: String?
)

enum class ComponentType { ACTIVITY, SERVICE, RECEIVER, PROVIDER }
```

---

### `ManifestSecurityReport`

Output di `ManifestSecurityAnalyzer.analyze()`.

```kotlin
enum class ManifestRiskLevel { LOW, MEDIUM, HIGH, CRITICAL }

data class ManifestSecurityFinding(
    val level: ManifestRiskLevel,
    val title: String,
    val detail: String
)

data class ManifestSecurityReport(
    val packageName: String,
    val riskLevel: ManifestRiskLevel,
    val riskScore: Int,
    val findings: List<ManifestSecurityFinding>    // sorted by severity
)
```

---

### `RootStatus`

Stato del root e Play Integrity del device.

```kotlin
data class RootStatus(
    val isRooted: Boolean,
    val rootIndicators: List<String>,
    val playIntegrityResult: PlayIntegrityResult?
)

data class PlayIntegrityResult(
    val passed: Boolean,
    val nonce: String,
    val details: Map<String, Boolean>
)
```

---

### `DeviceScanSnapshot`

Snapshot completo del device, usato per la comunicazione Android → Desktop.

```kotlin
data class DeviceScanSnapshot(
    val timestamp: Long,
    val deviceInfo: DeviceInfo,
    val rootStatus: RootStatus,
    val installedApps: List<AppEntry>,
    val permissions: List<AppPermissionEntry>,
    val activities: List<ActivityEntry>
)
```

---

### `ResultWrapper<T>`

Wrapper generico per risultati success/failure.

```kotlin
sealed class ResultWrapper<out T> {
    data class Success<T>(val data: T) : ResultWrapper<T>()
    data class Failure(val exception: Throwable) : ResultWrapper<Nothing>()
}
```

---

## Repository Interfaces

### `InstalledAppsRepository`

```kotlin
interface InstalledAppsRepository {
    suspend fun listInstalled(): List<AppEntry>
    suspend fun listInstalledPaginated(offset: Int, limit: Int): List<AppEntry>
    suspend fun analyzeManifestSecurity(packageName: String): AppManifestSecurity
    suspend fun analyzeManifestSecurityCached(packageName: String): AppManifestSecurity
    suspend fun exportApk(packageName: String): ResultWrapper<String>   // path file
    suspend fun shareApk(packageName: String)
}
```

---

### `ActivityRepository`

```kotlin
interface ActivityRepository {
    suspend fun getActivities(packageName: String): List<ActivityEntry>
    suspend fun getActivitiesPaginated(
        packageName: String,
        offset: Int,
        limit: Int
    ): List<ActivityEntry>
    suspend fun getComponents(packageName: String): List<AppComponent>
    suspend fun getComponentsCached(packageName: String): List<AppComponent>
    suspend fun launch(packageName: String, activityName: String): ResultWrapper<Unit>
}
```

---

### `PermissionsRepository`

```kotlin
interface PermissionsRepository {
    suspend fun listAppPermissions(packageName: String): AppPermissionEntry
    suspend fun listAppPermissionsPaginated(packageName: String, offset: Int, limit: Int): List<PermissionInfo>
}
```

---

### `RootCheckRepository`

```kotlin
interface RootCheckRepository {
    suspend fun checkRoot(): RootStatus
    suspend fun checkPlayIntegrity(nonce: String): PlayIntegrityResult
}
```

---

### `DeviceInfoRepository`

```kotlin
interface DeviceInfoRepository {
    suspend fun getDeviceInfo(): DeviceInfo
}
```

---

### `ShellCommandRepository`

```kotlin
interface ShellCommandRepository {
    suspend fun listCommands(): List<ShellCommand>
    suspend fun execute(command: ShellCommand): ShellCommandResult
}

data class ShellCommand(
    val id: String,
    val label: String,
    val command: String,
    val requiresRoot: Boolean,
    val requiresConfirmation: Boolean
)

data class ShellCommandResult(
    val exitCode: Int,
    val stdout: String,
    val stderr: String
)
```

---

### `LogRepository`

```kotlin
interface LogRepository {
    suspend fun fetchLog(): List<String>
}
```

L'esportazione dei log è gestita da `ExportLogsUseCase` tramite `ExportReportUseCase`, quindi
rispetta il formato documentale selezionato nelle impostazioni (`.txt`, `.json` o `.pdf`).

---

### `SettingsRepository`

```kotlin
interface SettingsRepository {
    suspend fun getTheme(): AppTheme
    suspend fun setTheme(theme: AppTheme)
    suspend fun getLanguage(): AppLanguage
    suspend fun setLanguage(language: AppLanguage)
    suspend fun isVerboseLog(): Boolean
    suspend fun setVerboseLog(enabled: Boolean)
    suspend fun isIntroSeen(): Boolean
    suspend fun setIntroSeen(seen: Boolean)
    // accessibilità
    suspend fun isHighContrast(): Boolean
    suspend fun setHighContrast(enabled: Boolean)
    suspend fun isReducedAnimations(): Boolean
    suspend fun setReducedAnimations(enabled: Boolean)
    suspend fun isLargeText(): Boolean
    suspend fun setLargeText(enabled: Boolean)
}
```

---

## Use Cases

All one-shot use-case operations are `suspend` and return Kotlin `Result<T>`,
including analysis and report generation. Setters return `Result<Unit>`.
Each class extends `BaseUseCase` and delegates its execution boundary to
`execute`: ordinary exceptions become failures, while `CancellationException`
is rethrown, including cancellation returned inside a repository `Result`.
Fatal `Error` instances are not converted to failures.

The base has no scope or job of its own. Repository operations retain the caller
context and repository-owned I/O dispatching; CPU analysis/report use cases
accept `AppDispatchers` and use `default`. Dispatchers live in `shared.util`.
Use cases accept a Kermit `Logger` for injectable diagnostics.

The signatures below omit constructors and method bodies.

### Permissions

```kotlin
class ListPermissionsUseCase {
    suspend operator fun invoke(): Result<List<AppPermissionEntry>>
}

class AnalyzePermissionRisksUseCase {
    suspend operator fun invoke(app: AppPermissionEntry): Result<PermissionRiskReport?>
    fun sortReports(reports: List<PermissionRiskReport>): List<PermissionRiskReport>
}

class BuildPermissionsReportUseCase {
    suspend operator fun invoke(
        apps: List<AppPermissionEntry>,
        riskReports: List<PermissionRiskReport>,
        text: UseCaseReportText
    ): Result<String>
    suspend fun single(
        app: AppPermissionEntry,
        riskReport: PermissionRiskReport?,
        text: UseCaseReportText
    ): Result<String>
    fun singleFileName(packageName: String): String
}
```

`Result.success(null)` from risk analysis means no findings, not failure.
Formatting helpers remain synchronous; execute sorting on the CPU dispatcher
when processing scan batches. Permission report preparation is asynchronous in
the ViewModel, with loading/content/error state; superseded requests are cancelled.

`PermissionRiskFinding` carries `key`, `kind: PermissionFindingKind`, `level` and
`isPattern`, not translated title/reason strings. `PenTestCheckResult` similarly
carries a typed `PenTestMessage` with numeric/technical parameters. Findings are
rendered by the pure `UseCaseTexts` catalog in Italian, English, German, Spanish
or French (unsupported languages fall back to English). Permission names and
technical terms remain unchanged.

`UseCaseReportText` is a domain contract implemented by that catalog, explicitly
supplied to report builders. UI and reports share finding and severity rendering;
no preference or Compose lookup happens in the domain. ViewModels retain semantic
results and remap them when language changes, including saved in-memory PenTest
snapshots, without repeating repository calls or scans. Preview requests in the
old language are cancelled; each export/share captures its resources at invocation.

### Activities

```kotlin
class GetActivitiesUseCase {
    suspend operator fun invoke(): Result<List<ActivityEntry>>
}
```

### InstalledApps

```kotlin
class ListInstalledAppsUseCase {
    suspend operator fun invoke(): Result<List<InstalledAppEntry>>
}

class ExportApkUseCase {
    suspend operator fun invoke(packageName: String): Result<String>
}
```

### Root

```kotlin
class CheckRootUseCase {
    suspend operator fun invoke(mode: RootCheckMode = RootCheckMode.PASSIVE): Result<RootStatus>
}
```

Negative root/Integrity verdicts and shell outcomes retain their domain-model
semantics; they are not automatically converted into exceptions.

### Reports and settings

```kotlin
class BuildDeviceInfoReportUseCase {
    suspend operator fun invoke(
        info: DeviceInfo,
        root: RootStatus?,
        text: UseCaseReportText
    ): Result<String>
}

class ExportReportUseCase {
    suspend fun save(fileName: String, content: String): Result<String>
    suspend fun share(fileName: String, content: String): Result<String>
}

class GetThemeUseCase {
    suspend operator fun invoke(): Result<AppTheme>
}

class SetThemeUseCase {
    suspend operator fun invoke(theme: AppTheme): Result<Unit>
}
```

Use `onSuccess`/`onFailure` directly for single operations. For composed
operations, unwrap each result with `getOrThrow()` inside a cancellation-safe
boundary; never ignore a failed setter or return nested `Result`.

### Diagnostics and future streams

`BaseUseCase` logs use-case name, operation, outcome and monotonic duration.
Debug events (start/success/cancellation) follow the persisted **Verbose logs**
preference, initialized before shell-triggered feature loads. Failures are
error-level events regardless of that preference. Only exception category is
logged: no raw message/cause, throwable, arguments, results, tokens, shell
commands or device/report data. Original exceptions remain available in `Result`.
Changing verbosity does not change unrelated Kermit/HTTP log levels.

There are currently no Flow use cases. Future observation operations should
use `Flow<Result<T>>`, not `Result<Flow<T>>`: upstream failures belong to the
stream boundary, and cancellation must propagate. Do not catch collector failures.

---

## ViewModels & State

### `PenTestingViewModel`

```kotlin
data class PenTestingState(
    val isRunning: Boolean = false,
    val progress: Float = 0f,
    val results: List<PenTestResult> = emptyList(),
    val history: List<PenTestHistoryEntry> = emptyList(),
    val savedReports: List<PenTestReport> = emptyList(),
    val error: String? = null
)

// Modalità di esecuzione
sealed class PenTestMode {
    data class SingleTarget(val packageName: String) : PenTestMode()
    object AutoAllApps : PenTestMode()
}

// API pubblica
class PenTestingViewModel : BaseViewModel() {
    val state: StateFlow<PenTestingState>

    fun runSingleTarget(packageName: String)
    fun runAutoScan()
    fun cancelScan()
    fun refresh()
    fun clearHistory()
    fun exportReport(report: PenTestReport)
    fun shareReport(report: PenTestReport)
}
```

---

### `InstalledAppsViewModel`

```kotlin
data class InstalledAppsState(
    val apps: List<AppEntry> = emptyList(),
    val filteredApps: List<AppEntry> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val favorites: Set<String> = emptySet(),    // packageNames
    val selectedApp: AppEntry? = null,
    val manifestReport: ManifestSecurityReport? = null,
    val error: String? = null
)
```

---

## Caching API

### `PaginationHelper`

```kotlin
object PaginationHelper {
    /**
     * Ritaglia [items] dalla posizione [offset] per [limit] elementi.
     * Ritorna lista vuota se offset >= size.
     */
    fun <T> paginate(items: List<T>, offset: Int, limit: Int): List<T>

    /**
     * Calcola il numero totale di pagine (ceiling division).
     */
    fun countPages(totalItems: Int, pageSize: Int): Int
}
```

### `CachingRepositoryDecorator`

```kotlin
class CachingRepositoryDecorator {
    val cacheValidityMs: Long = 60_000L  // TTL: 1 minuto

    /**
     * Ritorna il valore cached se valido, altrimenti invoca [compute] e lo memorizza.
     */
    suspend fun <T> getCachedOrCompute(cache: CacheEntry<T>?, compute: suspend () -> T): T

    // Manifest security cache
    fun cacheManifestSecurityReport(pkg: String, report: ManifestSecurityReport)
    fun getManifestSecurityCache(pkg: String): ManifestSecurityReport?
    fun invalidateManifestSecurityCache(pkg: String? = null)  // null = invalida tutto

    // Components cache
    fun cacheComponents(pkg: String, components: List<AppComponent>)
    fun getComponentsCache(pkg: String): List<AppComponent>?
    fun invalidateComponentsCache(pkg: String? = null)
}
```

---

## Utilities

### `ManifestSecurityAnalyzer`

```kotlin
object ManifestSecurityAnalyzer {
    /**
     * Analizza [security] e produce un ManifestSecurityReport con score e findings.
     * I findings sono ordinati per severità decrescente.
     */
    fun analyze(security: AppManifestSecurity): ManifestSecurityReport
}
```

### `TextExport` (expect/actual)

```kotlin
// commonMain — expect
expect suspend fun saveTextFile(content: String, filename: String): ResultWrapper<String>
expect suspend fun shareTextFile(content: String, filename: String)

// androidMain — actual: usa MediaStore / FileProvider
// desktopMain — actual: scrive in home directory / mostra file dialog
```

### `AppInfo` (expect/actual)

```kotlin
// commonMain — expect
expect val appVersionName: String
expect val appPackageName: String
expect val appSdkInfo: String
```
