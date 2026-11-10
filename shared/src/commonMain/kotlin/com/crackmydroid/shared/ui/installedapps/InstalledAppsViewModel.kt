package com.crackmydroid.shared.ui.installedapps

import com.crackmydroid.architecture.coroutines.AppDispatchers
import com.crackmydroid.shared.domain.model.InstalledAppEntry
import com.crackmydroid.shared.domain.repository.log.OperationLogRepository
import com.crackmydroid.shared.domain.usecase.installedapps.ExportApkUseCase
import com.crackmydroid.shared.domain.usecase.installedapps.ListInstalledAppsUseCase
import com.crackmydroid.shared.domain.usecase.installedapps.ShareApkUseCase
import com.crackmydroid.shared.domain.usecase.reports.ExportReportUseCase
import com.crackmydroid.shared.domain.usecase.settings.GetInstalledAppFavoritesUseCase
import com.crackmydroid.shared.domain.usecase.settings.SetInstalledAppFavoritesUseCase
import com.crackmydroid.shared.i18n.ScreenUiStrings
import com.crackmydroid.shared.i18n.format
import com.crackmydroid.shared.presentation.LocalizedBaseViewModel
import com.crackmydroid.shared.presentation.operations.NoOpOperationLogRepository
import com.crackmydroid.shared.util.runSuspendCatching
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement

class InstalledAppsViewModel(
    private val listUseCase: ListInstalledAppsUseCase,
    private val exportUseCase: ExportApkUseCase,
    private val shareUseCase: ShareApkUseCase,
    private val reportExporter: ExportReportUseCase,
    private val getFavorites: GetInstalledAppFavoritesUseCase,
    private val setFavorites: SetInstalledAppFavoritesUseCase,
    private val operationLogs: OperationLogRepository = NoOpOperationLogRepository,
    dispatchers: AppDispatchers,
) : LocalizedBaseViewModel<InstalledAppsEvent>(dispatchers) {
    private val _state = MutableStateFlow(InstalledAppsState())
    val state: StateFlow<InstalledAppsState> = _state
    private val queryCount = mutableMapOf<String, Int>()
    private val multiSpaceRegex = Regex("\\s+")
    private val uiStrings get() = ScreenUiStrings.from(strings)

    override fun onEvent(event: InstalledAppsEvent) = when (event) {
        is InstalledAppsEvent.Refresh -> refresh()
        is InstalledAppsEvent.FilterChanged -> setFilter(event.value, event.trackUsage)
        is InstalledAppsEvent.ApkExportRequested -> export(event.packageName)
        is InstalledAppsEvent.AllExportRequested -> exportAll(event.baseName)
        is InstalledAppsEvent.AllShareRequested -> shareAll(event.baseName)
        is InstalledAppsEvent.ApkShareRequested -> share(event.packageName, event.bluetoothOnly)
        is InstalledAppsEvent.ErrorDismissed -> clearError()
        is InstalledAppsEvent.FavoriteToggled -> toggleFavorite(event.packageName)
    }

    private fun refresh() {
        launchLatest("installed-apps-refresh") {
            val favoritesResult = getFavorites()
            ensureCurrent()
            favoritesResult.onSuccess { favorites ->
                updatePresentation { it.copy(favorites = favorites) }
            }
            _state.update {
                it.copy(loading = true, error = favoritesResult.exceptionOrNull()?.message)
            }
            listUseCase()
                .onSuccess { list ->
                    if (!isCurrent()) return@onSuccess
                    updatePresentation { it.copy(apps = list, loading = false) }
                    operationLogs.record("Caricate ${list.size} app installate", success = true)
                }
                .onFailure { err ->
                    if (!isCurrent()) return@onFailure
                    _state.update { it.copy(error = err.message ?: strings.commandError, loading = false) }
                    operationLogs.record(
                        "Caricamento app installate",
                        success = false,
                        details = err.message,
                    )
                }
        }
    }

    private fun setFilter(value: String, trackUsage: Boolean = false) {
        if (trackUsage) {
            val normalized = normalizeQuery(value)
            if (normalized.length >= 2) {
                val count = (queryCount[normalized] ?: 0) + 1
                queryCount[normalized] = count
            }
            val top = queryCount.maxByOrNull { it.value }?.key
            _state.update { it.copy(topQuery = top) }
        }
        updatePresentation { it.copy(filter = value) }
    }

    private fun visibleApps(state: InstalledAppsState): List<InstalledAppEntry> {
        val f = state.filter
        val filtered = if (f.isBlank()) {
            state.apps
        } else {
            state.apps.filter {
                it.appLabel.contains(f, true) ||
                    it.packageName.contains(f, true) ||
                    it.sourcePath.contains(f, true)
            }
        }
        val favs = state.favorites
        return filtered.sortedWith(
            compareBy<InstalledAppEntry> { if (favs.contains(it.packageName)) 0 else 1 }
                .thenBy { it.appLabel.lowercase() }
                .thenBy { it.packageName.lowercase() },
        )
    }

    private fun export(packageName: String) {
        launchTask {
            _state.update { it.copy(error = null) }
            val result = exportUseCase(packageName)
            result
                .onSuccess { path ->
                    _state.update { it.copy(lastExportPath = path, lastSharedPackage = null) }
                    operationLogs.record("Export APK $packageName", success = true, details = path)
                }
                .onFailure { err ->
                    _state.update { it.copy(error = err.message ?: strings.commandError) }
                    operationLogs.record(
                        "Export APK $packageName",
                        success = false,
                        details = err.message,
                    )
                }
        }
    }

    private fun exportAll(baseName: String = "installed_apps") {
        launchTask {
            _state.update { it.copy(error = null) }
            runSuspendCatching {
                val report = buildListReport(baseName)
                reportExporter.save(baseName, "installed_apps", report.textContent, report.data).getOrThrow()
            }
                .onSuccess { path ->
                    _state.update { it.copy(lastExportPath = path, error = null) }
                    operationLogs.record("Export lista app", success = true, details = path)
                }
                .onFailure { err ->
                    _state.update { it.copy(error = err.message) }
                    operationLogs.record("Export lista app", success = false, details = err.message)
                }
        }
    }

    private fun shareAll(baseName: String = "installed_apps") {
        launchTask {
            _state.update { it.copy(error = null) }
            runSuspendCatching {
                val report = buildListReport(baseName)
                reportExporter.share(baseName, "installed_apps", report.textContent, report.data).getOrThrow()
            }
                .onSuccess { path ->
                    _state.update { it.copy(lastExportPath = path, error = null) }
                    operationLogs.record("Share lista app", success = true, details = path)
                }
                .onFailure { err ->
                    _state.update { it.copy(error = err.message) }
                    operationLogs.record("Share lista app", success = false, details = err.message)
                }
        }
    }

    private fun share(packageName: String, bluetoothOnly: Boolean) {
        launchTask {
            _state.update { it.copy(error = null) }
            val result = shareUseCase(packageName, bluetoothOnly)
            result
                .onSuccess {
                    _state.update { it.copy(lastSharedPackage = packageName) }
                    operationLogs.record("Share APK $packageName", success = true)
                }
                .onFailure { err ->
                    _state.update { it.copy(error = err.message ?: strings.commandError) }
                    operationLogs.record(
                        "Share APK $packageName",
                        success = false,
                        details = err.message,
                    )
                }
        }
    }

    private fun clearError() {
        _state.update { it.copy(error = null) }
    }

    private fun toggleFavorite(packageName: String) {
        updatePresentation {
            val favs = it.favorites.toMutableSet()
            if (favs.contains(packageName)) favs.remove(packageName) else favs.add(packageName)
            it.copy(favorites = favs)
        }
        launchTask {
            setFavorites(_state.value.favorites)
                .onFailure { error -> _state.update { it.copy(error = error.message) } }
        }
    }

    private fun updatePresentation(transform: (InstalledAppsState) -> InstalledAppsState) {
        _state.update { current ->
            val updated = transform(current)
            updated.copy(
                visibleApps = visibleApps(updated),
                favoriteQueries = updated.favorites.sorted(),
            )
        }
    }

    private suspend fun buildListReport(baseName: String): GeneratedReport {
        val sortedApps = _state.value.apps.sortedBy { it.appLabel.lowercase() }
        val content = buildString {
            appendLine(uiStrings.installedAppsReportTitle)
            appendLine("==============================================")
            appendLine(uiStrings.installedAppsTotal.format(sortedApps.size))
            appendLine(uiStrings.readableTextFormat)
            appendLine()
            sortedApps.forEachIndexed { index, app ->
                appendLine("${index + 1}) ${app.appLabel}")
                appendLine("${uiStrings.packageLabel}: ${app.packageName}")
                appendLine("${uiStrings.apkPathLabel}: ${app.sourcePath}")
                appendLine("${uiStrings.installSourceLabel}: ${app.installSource.classification}")
                app.installSource.installingPackageName?.let { appendLine("${uiStrings.installerLabel}: $it") }
                appendLine()
            }
        }
        return GeneratedReport(
            textContent = content,
            data = Json.encodeToJsonElement(sortedApps),
        )
    }

    private fun normalizeQuery(value: String): String = value
        .trim()
        .replace(multiSpaceRegex, " ")
        .lowercase()

    private data class GeneratedReport(val textContent: String, val data: JsonElement)
}
