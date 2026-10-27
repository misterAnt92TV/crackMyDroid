package com.crackmydroid.shared.ui.installedapps

import com.crackmydroid.shared.domain.model.InstalledAppEntry
import com.crackmydroid.shared.domain.usecase.ExportApkUseCase
import com.crackmydroid.shared.domain.usecase.ListInstalledAppsUseCase
import com.crackmydroid.shared.domain.usecase.ShareApkUseCase
import com.crackmydroid.shared.presentation.BaseViewModel
import com.crackmydroid.shared.domain.repository.OperationLogRepository
import com.crackmydroid.shared.presentation.operations.NoOpOperationLogRepository
import com.crackmydroid.shared.domain.usecase.GetExportFormatUseCase
import com.crackmydroid.shared.domain.usecase.reports.ExportReportUseCase
import com.crackmydroid.shared.domain.model.ExportFormat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.crackmydroid.shared.domain.usecase.GetInstalledAppFavoritesUseCase
import com.crackmydroid.shared.domain.usecase.SetInstalledAppFavoritesUseCase

class InstalledAppsViewModel(
    private val listUseCase: ListInstalledAppsUseCase,
    private val exportUseCase: ExportApkUseCase,
    private val shareUseCase: ShareApkUseCase,
    private val getExportFormat: GetExportFormatUseCase,
    private val reportExporter: ExportReportUseCase,
    private val getFavorites: GetInstalledAppFavoritesUseCase? = null,
    private val setFavorites: SetInstalledAppFavoritesUseCase? = null,
    private val operationLogs: OperationLogRepository = NoOpOperationLogRepository
) : BaseViewModel() {
    private val _state = MutableStateFlow(InstalledAppsState())
    val state: StateFlow<InstalledAppsState> = _state
    private val queryCount = mutableMapOf<String, Int>()
    private val multiSpaceRegex = Regex("\\s+")

    fun refresh() {
        scope.launch {
            runCatching { getFavorites?.invoke().orEmpty() }.onSuccess { favorites -> _state.update { it.copy(favorites = favorites) } }
            _state.update { it.copy(loading = true, error = null) }
            runCatching { listUseCase() }
                .onSuccess { list ->
                    _state.update { it.copy(apps = list, loading = false) }
                    operationLogs.record("Caricate ${list.size} app installate", success = true)
                }
                .onFailure { err ->
                    _state.update { it.copy(error = err.message ?: "Errore", loading = false) }
                    operationLogs.record("Caricamento app installate", success = false, details = err.message)
                }
        }
    }

    fun setFilter(value: String, trackUsage: Boolean = false) {
        if (trackUsage) {
            val normalized = normalizeQuery(value)
            if (normalized.length >= 2) {
                val count = (queryCount[normalized] ?: 0) + 1
                queryCount[normalized] = count
            }
            val top = queryCount.maxByOrNull { it.value }?.key
            _state.update { it.copy(topQuery = top) }
        }
        _state.update { it.copy(filter = value) }
    }

    fun filtered(): List<InstalledAppEntry> {
        val f = _state.value.filter
        val filtered = if (f.isBlank()) {
            _state.value.apps
        } else {
            _state.value.apps.filter {
                it.appLabel.contains(f, true) ||
                    it.packageName.contains(f, true) ||
                    it.sourcePath.contains(f, true)
            }
        }
        val favs = _state.value.favorites
        return filtered.sortedWith(
            compareBy<InstalledAppEntry> { if (favs.contains(it.packageName)) 0 else 1 }
                .thenBy { it.appLabel.lowercase() }
                .thenBy { it.packageName.lowercase() }
        )
    }

    fun export(packageName: String) {
        scope.launch {
            _state.update { it.copy(error = null) }
            val result = exportUseCase(packageName)
            result
                .onSuccess { path ->
                    _state.update { it.copy(lastExportPath = path, lastSharedPackage = null) }
                    operationLogs.record("Export APK $packageName", success = true, details = path)
                }
                .onFailure { err ->
                    _state.update { it.copy(error = err.message ?: "Errore") }
                    operationLogs.record("Export APK $packageName", success = false, details = err.message)
                }
        }
    }

    fun exportAll(baseName: String = "installed_apps") {
        scope.launch {
            _state.update { it.copy(error = null) }
            val report = buildListReport(baseName)
            reportExporter.save(report.fileName, report.content)
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

    fun shareAll(baseName: String = "installed_apps") {
        scope.launch {
            _state.update { it.copy(error = null) }
            val report = buildListReport(baseName)
            reportExporter.share(report.fileName, report.content).onSuccess { path ->
                _state.update { it.copy(lastExportPath = path, error = null) }
                operationLogs.record("Share lista app", success = true, details = path)
            }.onFailure { err ->
                _state.update { it.copy(error = err.message) }
                operationLogs.record("Share lista app", success = false, details = err.message)
            }
        }
    }

    fun share(packageName: String, bluetoothOnly: Boolean) {
        scope.launch {
            _state.update { it.copy(error = null) }
            val result = shareUseCase(packageName, bluetoothOnly)
            result
                .onSuccess {
                    _state.update { it.copy(lastSharedPackage = packageName) }
                    operationLogs.record("Share APK $packageName", success = true)
                }
                .onFailure { err ->
                    _state.update { it.copy(error = err.message ?: "Errore") }
                    operationLogs.record("Share APK $packageName", success = false, details = err.message)
                }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun toggleFavorite(packageName: String) {
        _state.update {
            val favs = it.favorites.toMutableSet()
            if (favs.contains(packageName)) favs.remove(packageName) else favs.add(packageName)
            it.copy(favorites = favs)
        }
        scope.launch { setFavorites?.invoke(_state.value.favorites) }
    }

    private suspend fun buildListReport(baseName: String): GeneratedReport {
        val format = runCatching { getExportFormat() }.getOrDefault(ExportFormat.TXT)
        val sortedApps = _state.value.apps.sortedBy { it.appLabel.lowercase() }
        val content = if (format == ExportFormat.JSON) {
            buildString {
                appendLine("[")
                sortedApps.forEachIndexed { idx, app ->
                    append("  {\"label\":\"${app.appLabel}\",\"package\":\"${app.packageName}\",\"path\":\"${app.sourcePath}\"}")
                    if (idx != sortedApps.lastIndex) append(",")
                    appendLine()
                }
                append("]")
            }
        } else {
            buildString {
                appendLine("REPORT APP INSTALLATE")
                appendLine("==============================================")
                appendLine("Totale app: ${sortedApps.size}")
                appendLine("Formato: testo leggibile")
                appendLine()
                sortedApps.forEachIndexed { index, app ->
                    appendLine("${index + 1}) ${app.appLabel}")
                    appendLine("Package: ${app.packageName}")
                    appendLine("Path APK: ${app.sourcePath}")
                    appendLine()
                }
            }
        }
        return GeneratedReport(
            fileName = baseName + if (format == ExportFormat.JSON) ".json" else "",
            content = content
        )
    }

    private fun normalizeQuery(value: String): String = value
        .trim()
        .replace(multiSpaceRegex, " ")
        .lowercase()

    private data class GeneratedReport(
        val fileName: String,
        val content: String
    )
}
