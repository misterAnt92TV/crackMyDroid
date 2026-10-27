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
    suspend fun getActivitiesPaginated(packageName: String, offset: Int, limit: Int): List<ActivityEntry>
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
    suspend fun fetchLog(): List<LogEntry>
    suspend fun export(): ResultWrapper<String>   // path file esportato
}
```

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

### Permissions

```kotlin
class ListPermissionsUseCase(private val repo: PermissionsRepository) {
    suspend operator fun invoke(packageName: String): AppPermissionEntry
}

class AnalyzePermissionRisksUseCase {
    operator fun invoke(entry: AppPermissionEntry): PermissionRiskReport
}

class BuildPermissionsReportUseCase {
    operator fun invoke(report: PermissionRiskReport): String   // testo esportabile
}
```

### Activities

```kotlin
class GetActivitiesUseCase(private val repo: ActivityRepository) {
    suspend operator fun invoke(packageName: String): List<ActivityEntry>
}
```

### InstalledApps

```kotlin
class ListInstalledAppsUseCase(private val repo: InstalledAppsRepository) {
    suspend operator fun invoke(): List<AppEntry>
}

class ExportApkUseCase(private val repo: InstalledAppsRepository) {
    suspend operator fun invoke(packageName: String): ResultWrapper<String>
}
```

### Root

```kotlin
class CheckRootUseCase(private val repo: RootCheckRepository) {
    suspend operator fun invoke(): RootStatus
}
```

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
