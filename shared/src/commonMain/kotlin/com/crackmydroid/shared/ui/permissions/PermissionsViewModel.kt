package com.crackmydroid.shared.ui.permissions

import com.crackmydroid.shared.domain.model.app.AppPermissionEntry
import com.crackmydroid.shared.domain.model.PermissionRiskReport
import com.crackmydroid.shared.domain.usecase.permissions.ListPermissionsUseCase
import com.crackmydroid.shared.domain.usecase.settings.SetPermissionFavoritesUseCase
import com.crackmydroid.shared.domain.usecase.permissions.AnalyzePermissionRisksUseCase
import com.crackmydroid.shared.domain.usecase.permissions.BuildPermissionsReportUseCase
import com.crackmydroid.shared.domain.usecase.reports.ExportReportUseCase
import com.crackmydroid.shared.domain.usecase.settings.GetPermissionFavoritesUseCase
import com.crackmydroid.shared.presentation.BaseViewModel
import com.crackmydroid.shared.i18n.UseCaseTexts
import com.crackmydroid.shared.i18n.UseCasePresentationTexts
import com.crackmydroid.shared.util.AppDispatchers
import com.crackmydroid.shared.util.DefaultAppDispatchers
import com.crackmydroid.shared.util.runSuspendCatching
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PermissionsViewModel(
    private val listPermissions: ListPermissionsUseCase,
    private val analyzePermissionRisks: AnalyzePermissionRisksUseCase,
    private val buildPermissionsReport: BuildPermissionsReportUseCase,
    private val reportExporter: ExportReportUseCase,
    private val getFavorites: GetPermissionFavoritesUseCase,
    private val setFavorites: SetPermissionFavoritesUseCase,
    dispatchers: AppDispatchers = DefaultAppDispatchers
) : BaseViewModel(dispatchers) {
    private val _state = MutableStateFlow(PermissionsState())
    val state: StateFlow<PermissionsState> = _state
    private val queryCount = mutableMapOf<String, Int>()
    private var riskScanJob: Job? = null
    private var reportPreviewJob: Job? = null
    private var reportPreviewId = 0L
    private val multiSpaceRegex = Regex("\\s+")
    private var text = UseCaseTexts("en")
    private var copy = UseCasePresentationTexts("en")

    fun updateLanguage(languageCode: String) {
        val next = UseCaseTexts(languageCode)
        if (text.languageCode == next.languageCode) return
        val previewPackage = _state.value.reportPreviewPackage
        dismissReportPreview()
        text = next
        copy = UseCasePresentationTexts(next.languageCode)
        updatePresentation { it }
        if (previewPackage != null) requestReportPreview(previewPackage)
    }

    fun refresh() {
        cancelAutoRiskScan()
        dismissReportPreview()
        scope.launch {
            val favoritesResult = getFavorites()
            favoritesResult.onSuccess { favorites ->
                updatePresentation { it.copy(favorites = favorites) }
            }
            _state.update {
                it.copy(
                    loading = true,
                    error = favoritesResult.exceptionOrNull()?.message,
                    autoScanRunning = false,
                    autoScanTotalApps = 0,
                    autoScanScannedApps = 0,
                    autoScanCurrentLabel = null,
                    showOnlyRisky = false,
                    riskReports = emptyList(),
                    findingsByPackage = emptyMap(),
                    visibleFindingsByPackage = emptyMap()
                )
            }
            listPermissions()
                .onSuccess { apps -> updatePresentation { it.copy(apps = apps, loading = false) } }
                .onFailure { err ->
                    _state.update {
                        it.copy(
                            error = err.message,
                            loading = false
                        )
                    }
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
        updatePresentation { it.copy(filter = value) }
    }

    fun setShowOnlyRisky(enabled: Boolean) {
        updatePresentation { it.copy(showOnlyRisky = enabled) }
    }

    fun toggleShowOnlyRisky() {
        updatePresentation { it.copy(showOnlyRisky = !it.showOnlyRisky) }
    }

    private fun visibleApps(state: PermissionsState): List<AppPermissionEntry> {
        val filterValue = state.filter.trim()
        val riskScoreByPackage = state.riskReports.associate { it.packageName to it.score }
        val riskyPackages = state.riskReports.mapTo(mutableSetOf()) { it.packageName }

        val textFiltered = if (filterValue.isBlank()) {
            state.apps
        } else {
            state.apps.filter {
                it.appLabel.contains(filterValue, true) ||
                        it.packageName.contains(filterValue, true) ||
                        it.permissions.any { permission -> permission.contains(filterValue, true) }
            }
        }

        val riskFiltered = if (state.showOnlyRisky && riskyPackages.isNotEmpty()) {
            textFiltered.filter { riskyPackages.contains(it.packageName) }
        } else {
            textFiltered
        }

        val favorites = state.favorites
        return riskFiltered.sortedWith(
            compareByDescending<AppPermissionEntry> { favorites.contains(it.packageName) }
                .thenByDescending { riskScoreByPackage[it.packageName] ?: 0 }
                .thenBy { it.appLabel.lowercase() }
        )
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun export() {
        val reportText = text
        scope.launch {
            val currentState = _state.value
            runSuspendCatching {
                val content = buildPermissionsReport(currentState.apps, currentState.riskReports, reportText).getOrThrow()
                reportExporter.save("permissions_report.txt", content).getOrThrow()
            }
                .onSuccess { path ->
                    _state.update {
                        it.copy(
                            lastExportPath = path,
                            error = null
                        )
                    }
                }
                .onFailure { error -> _state.update { it.copy(error = error.message) } }
        }
    }

    fun toggleFavorite(packageName: String) {
        updatePresentation {
            val favorites = it.favorites.toMutableSet()
            if (favorites.contains(packageName)) favorites.remove(packageName) else favorites.add(
                packageName
            )
            it.copy(favorites = favorites)
        }
        scope.launch {
            setFavorites(_state.value.favorites)
                .onFailure { error -> _state.update { it.copy(error = error.message) } }
        }
    }

    fun shareSingle(packageName: String, contentOverride: String? = null) {
        val reportText = text
        val noData = copy.noPackageData
        scope.launch {
            val currentState = _state.value
            val app = currentState.apps.find { it.packageName == packageName }
            val risk = currentState.riskReports.find { it.packageName == packageName }
            runSuspendCatching {
                val content = contentOverride ?: app?.let { buildPermissionsReport.single(it, risk, reportText).getOrThrow() }
                    ?: noData
                reportExporter.share(buildPermissionsReport.singleFileName(packageName), content).getOrThrow()
            }
                .onSuccess { path ->
                    _state.update {
                        it.copy(
                            lastExportPath = path,
                            error = null
                        )
                    }
                }
                .onFailure { error -> _state.update { it.copy(error = error.message) } }
        }
    }

    fun requestReportPreview(packageName: String) {
        reportPreviewJob?.cancel()
        val requestId = ++reportPreviewId
        val currentState = _state.value
        val app = currentState.apps.find { it.packageName == packageName }
        val risk = currentState.riskReports.find { it.packageName == packageName }
        val reportText = text
        val reportCopy = copy
        _state.update {
            it.copy(
                reportPreviewPackage = packageName,
                reportPreviewLoading = true,
                reportPreviewContent = null,
                reportPreviewError = null
            )
        }
        reportPreviewJob = scope.launch {
            val result = if (app == null) {
                Result.success(reportCopy.noPackageData)
            } else {
                buildPermissionsReport.single(app, risk, reportText)
            }
            if (requestId != reportPreviewId) return@launch
            result.onSuccess { content ->
                _state.update { it.copy(reportPreviewLoading = false, reportPreviewContent = content) }
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        reportPreviewLoading = false,
                        reportPreviewError = error.message ?: reportCopy.reportFailed
                    )
                }
            }
        }
    }

    fun dismissReportPreview() {
        ++reportPreviewId
        reportPreviewJob?.cancel()
        reportPreviewJob = null
        _state.update {
            it.copy(
                reportPreviewPackage = null,
                reportPreviewLoading = false,
                reportPreviewContent = null,
                reportPreviewError = null
            )
        }
    }

    fun startAutoRiskScan() {
        val apps = _state.value.apps
        if (apps.isEmpty()) {
            _state.update { it.copy(error = copy.noPermissionApps) }
            return
        }

        riskScanJob?.cancel()
        riskScanJob = null

        _state.update {
            it.copy(
                error = null,
                autoScanRunning = true,
                autoScanTotalApps = apps.size,
                autoScanScannedApps = 0,
                autoScanCurrentLabel = apps.firstOrNull()?.appLabel,
                showOnlyRisky = false,
                riskReports = emptyList(),
                findingsByPackage = emptyMap(),
                visibleFindingsByPackage = emptyMap()
            )
        }

        riskScanJob = scope.launch {
            val results = mutableListOf<PermissionRiskReport>()
            try {
                apps.forEachIndexed { index, app ->
                    _state.update { it.copy(autoScanCurrentLabel = app.appLabel) }
                    analyzePermissionRisks(app).getOrThrow()?.let { results += it }
                    val sorted = withContext(dispatchers.default) {
                        analyzePermissionRisks.sortReports(results)
                    }
                    updatePresentation {
                        it.copy(
                            autoScanScannedApps = index + 1,
                            riskReports = sorted
                        )
                    }
                    delay(40)
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (failure: Exception) {
                _state.update { it.copy(error = failure.message ?: copy.permissionScanFailed) }
            } finally {
                _state.update {
                    it.copy(
                        autoScanRunning = false,
                        autoScanCurrentLabel = null
                    )
                }
            }
        }
    }

    fun cancelAutoRiskScan() {
        riskScanJob?.cancel()
        riskScanJob = null
        _state.update {
            it.copy(
                autoScanRunning = false,
                autoScanCurrentLabel = null
            )
        }
    }

    private fun normalizeQuery(value: String): String = value
        .trim()
        .replace(multiSpaceRegex, " ")
        .lowercase()

    private fun updatePresentation(transform: (PermissionsState) -> PermissionsState) {
        _state.update { current ->
            val updated = transform(current)
            val findings = updated.riskReports.associate { report ->
                report.packageName to report.findings.map { it.toUi(text) }
            }
            updated.copy(
                visibleApps = visibleApps(updated),
                favoriteQueries = updated.favorites.sorted(),
                findingsByPackage = findings,
                visibleFindingsByPackage = findings.mapValues { (_, values) -> values.filterNot { it.isPattern } }
            )
        }
    }
}
