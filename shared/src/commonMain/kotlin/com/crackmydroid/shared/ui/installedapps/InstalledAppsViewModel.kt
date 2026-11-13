package com.crackmydroid.shared.ui.installedapps

import com.crackmydroid.architecture.coroutines.AppDispatchers
import com.crackmydroid.architecture.presentation.model.UiLoadState
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
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.coroutines.withContext

class InstalledAppsViewModel(
    private val listInstalledAppsUseCase: ListInstalledAppsUseCase,
    private val exportApkUseCase: ExportApkUseCase,
    private val shareApkUseCase: ShareApkUseCase,
    private val reportExporterUseCase: ExportReportUseCase,
    private val getFavoritesUseCase: GetInstalledAppFavoritesUseCase,
    private val setFavoritesUseCase: SetInstalledAppFavoritesUseCase,
    private val operationLogs: OperationLogRepository = NoOpOperationLogRepository,
    dispatchers: AppDispatchers,
) : LocalizedBaseViewModel<InstalledAppsEvent, InstalledAppsState>(InstalledAppsState(), dispatchers) {
    private companion object {
        const val REFRESH_KEY = "installed-apps-refresh"
        const val RECOMPUTE_KEY = "installed-apps-recompute"
    }

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
        launchLatest(REFRESH_KEY) {
            val favoritesResult = getFavoritesUseCase()
            ensureCurrent()
            favoritesResult.onSuccess { favorites ->
                updatePresentation { it.copy(favorites = favorites) }
            }
            updateState {
                it.copy(loadState = UiLoadState.Loading)
            }
            listInstalledAppsUseCase()
                .onSuccess { list ->
                    if (!isCurrent()) return@onSuccess
                    updatePresentation { it.copy(apps = list, loadState = UiLoadState.Success) }
                    operationLogs.record(uiStrings.appsLoaded.format(list.size), success = true)
                }
                .onFailure { err ->
                    if (!isCurrent()) return@onFailure
                    updateState {
                        it.copy(
                            loadState = UiLoadState.Error(err.message ?: strings.commandError),
                        )
                    }
                    operationLogs.record(
                        uiStrings.loadingInstalledApps,
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
            updateState { it.copy(topQuery = top) }
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
            updateState { it.copy(error = null) }
            val result = exportApkUseCase(packageName)
            result
                .onSuccess { path ->
                    updateState { it.copy(lastExportPath = path, lastSharedPackage = null) }
                    operationLogs.record("${uiStrings.exportApk} $packageName", success = true, details = path)
                }
                .onFailure { err ->
                    updateState { it.copy(error = err.message ?: strings.commandError) }
                    operationLogs.record(
                        "${uiStrings.exportApk} $packageName",
                        success = false,
                        details = err.message,
                    )
                }
        }
    }

    private fun exportAll(baseName: String = "installed_apps") {
        launchTask {
            updateState { it.copy(error = null) }
            runSuspendCatching {
                val report = buildListReport(baseName)
                reportExporterUseCase.save(baseName, "installed_apps", report.textContent, report.data).getOrThrow()
            }
                .onSuccess { path ->
                    updateState { it.copy(lastExportPath = path, error = null) }
                    operationLogs.record(uiStrings.exportApps, success = true, details = path)
                }
                .onFailure { err ->
                    updateState { it.copy(error = err.message) }
                    operationLogs.record(uiStrings.exportApps, success = false, details = err.message)
                }
        }
    }

    private fun shareAll(baseName: String = "installed_apps") {
        launchTask {
            updateState { it.copy(error = null) }
            runSuspendCatching {
                val report = buildListReport(baseName)
                reportExporterUseCase.share(baseName, "installed_apps", report.textContent, report.data).getOrThrow()
            }
                .onSuccess { path ->
                    updateState { it.copy(lastExportPath = path, error = null) }
                    operationLogs.record(uiStrings.shareApps, success = true, details = path)
                }
                .onFailure { err ->
                    updateState { it.copy(error = err.message) }
                    operationLogs.record(uiStrings.shareApps, success = false, details = err.message)
                }
        }
    }

    private fun share(packageName: String, bluetoothOnly: Boolean) {
        launchTask {
            updateState { it.copy(error = null) }
            val result = shareApkUseCase(packageName, bluetoothOnly)
            result
                .onSuccess {
                    updateState { it.copy(lastSharedPackage = packageName) }
                    operationLogs.record("${uiStrings.shareApk} $packageName", success = true)
                }
                .onFailure { err ->
                    updateState { it.copy(error = err.message ?: strings.commandError) }
                    operationLogs.record(
                        "${uiStrings.shareApk} $packageName",
                        success = false,
                        details = err.message,
                    )
                }
        }
    }

    private fun clearError() {
        updateState {
            it.copy(
                error = null,
                loadState = if (it.loadState is UiLoadState.Error) UiLoadState.Idle else it.loadState,
            )
        }
    }

    private fun toggleFavorite(packageName: String) {
        updatePresentation {
            val favs = it.favorites.toMutableSet()
            if (favs.contains(packageName)) favs.remove(packageName) else favs.add(packageName)
            it.copy(favorites = favs)
        }
        launchTask {
            setFavoritesUseCase(currentState.favorites)
                .onFailure { error -> updateState { it.copy(error = error.message) } }
        }
    }

    private fun updatePresentation(transform: (InstalledAppsState) -> InstalledAppsState) {
        val transformed = transform(currentState)
        val updated = transformed.copy(favoriteQueries = transformed.favorites.sorted())
        if (updated == currentState) return
        emitState(updated)
        launchLatest(RECOMPUTE_KEY) {
            val recomputed = withContext(dispatchers.default) {
                updated.copy(visibleApps = visibleApps(updated))
            }
            ensureCurrent()
            emitState(recomputed)
        }
    }

    private suspend fun buildListReport(baseName: String): GeneratedReport {
        val sortedApps = currentState.apps.sortedBy { it.appLabel.lowercase() }
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
