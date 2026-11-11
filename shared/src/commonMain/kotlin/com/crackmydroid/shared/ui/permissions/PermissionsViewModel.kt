package com.crackmydroid.shared.ui.permissions

import com.crackmydroid.architecture.coroutines.AppDispatchers
import com.crackmydroid.shared.domain.model.PermissionRiskReport
import com.crackmydroid.shared.domain.model.app.AppPermissionEntry
import com.crackmydroid.shared.domain.usecase.permissions.AnalyzePermissionRisksUseCase
import com.crackmydroid.shared.domain.usecase.permissions.BuildPermissionsReportUseCase
import com.crackmydroid.shared.domain.usecase.permissions.ListPermissionsUseCase
import com.crackmydroid.shared.domain.usecase.reports.ExportReportUseCase
import com.crackmydroid.shared.domain.usecase.settings.GetPermissionFavoritesUseCase
import com.crackmydroid.shared.domain.usecase.settings.SetPermissionFavoritesUseCase
import com.crackmydroid.shared.presentation.LocalizedBaseViewModel
import com.crackmydroid.shared.util.runSuspendCatching
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

class PermissionsViewModel(
    private val listPermissions: ListPermissionsUseCase,
    private val analyzePermissionRisks: AnalyzePermissionRisksUseCase,
    private val buildPermissionsReport: BuildPermissionsReportUseCase,
    private val reportExporter: ExportReportUseCase,
    private val getFavorites: GetPermissionFavoritesUseCase,
    private val setFavorites: SetPermissionFavoritesUseCase,
    dispatchers: AppDispatchers,
) : LocalizedBaseViewModel<PermissionsEvent>(dispatchers) {
    private companion object {
        const val REFRESH_KEY = "permissions-refresh"
        const val RISK_SCAN_KEY = "permissions-risk-scan"
        const val REPORT_PREVIEW_KEY = "permissions-preview"
    }

    private val _state = MutableStateFlow(PermissionsState())
    val state: StateFlow<PermissionsState> = _state
    private val _effects = MutableSharedFlow<PermissionsEffect>()
    val effects: SharedFlow<PermissionsEffect> = _effects
    private val queryCount = mutableMapOf<String, Int>()
    private val multiSpaceRegex = Regex("\\s+")

    override fun onEvent(event: PermissionsEvent) = when (event) {
        is PermissionsEvent.Refresh -> refresh()

        is PermissionsEvent.FilterChanged -> setFilter(event.value, event.trackUsage)

        is PermissionsEvent.RiskFilterChanged -> setShowOnlyRisky(event.enabled)

        is PermissionsEvent.RiskFilterToggled -> toggleShowOnlyRisky()

        is PermissionsEvent.ErrorDismissed -> clearError()

        is PermissionsEvent.ExportRequested -> export()

        is PermissionsEvent.FavoriteToggled -> toggleFavorite(event.packageName)

        is PermissionsEvent.ReportShared -> shareSingle(
            event.packageName,
            event.contentOverride ?: _state.value.reportPreviewContent,
        )

        is PermissionsEvent.ReportPreviewRequested -> requestReportPreview(event.packageName)

        is PermissionsEvent.ReportPreviewDismissed -> dismissReportPreview()

        is PermissionsEvent.AutoRiskScanStarted -> startAutoRiskScan()

        is PermissionsEvent.AutoRiskScanCancelled -> cancelAutoRiskScan()
    }
    override fun onLanguageChanged() {
        val previewPackage = _state.value.reportPreviewPackage
        dismissReportPreview()
        updatePresentation(languageChanged = true) { it }
        if (previewPackage != null) requestReportPreview(previewPackage)
    }

    private fun refresh() {
        cancelAutoRiskScan()
        dismissReportPreview()
        cancelRefresh()
        launchLatest(REFRESH_KEY) {
            val favoritesResult = getFavorites()
            ensureCurrent()
            favoritesResult.onSuccess { favorites ->
                updatePresentation { it.copy(favorites = favorites) }
            }
            updatePresentation {
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
                    visibleFindingsByPackage = emptyMap(),
                )
            }
            val result = listPermissions()
            ensureCurrent()
            result
                .onSuccess { apps -> updatePresentation { it.copy(apps = apps, loading = false) } }
                .onFailure { err ->
                    _state.update {
                        it.copy(
                            error = err.message,
                            loading = false,
                        )
                    }
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

    private fun setShowOnlyRisky(enabled: Boolean) {
        updatePresentation { it.copy(showOnlyRisky = enabled) }
    }

    private fun toggleShowOnlyRisky() {
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
                .thenBy { it.appLabel.lowercase() },
        )
    }

    private fun clearError() {
        _state.update { it.copy(error = null) }
    }

    private fun export() {
        val reportText = useCaseTexts
        launchTask {
            val currentState = _state.value
            runSuspendCatching {
                val content = buildPermissionsReport(
                    currentState.apps,
                    currentState.riskReports,
                    reportText,
                ).getOrThrow()
                reportExporter.saveTextReport("permissions_report", "permissions", content).getOrThrow()
            }
                .onSuccess { path ->
                    _state.update {
                        it.copy(
                            lastExportPath = path,
                            error = null,
                        )
                    }
                }
                .onFailure { error -> _state.update { it.copy(error = error.message) } }
        }
    }

    private fun toggleFavorite(packageName: String) {
        updatePresentation {
            val favorites = it.favorites.toMutableSet()
            if (favorites.contains(packageName)) {
                favorites.remove(packageName)
            } else {
                favorites.add(
                    packageName,
                )
            }
            it.copy(favorites = favorites)
        }
        launchTask {
            setFavorites(_state.value.favorites)
                .onFailure { error -> _state.update { it.copy(error = error.message) } }
        }
    }

    private fun shareSingle(packageName: String, contentOverride: String? = null) {
        val reportText = useCaseTexts
        val noData = presentationTexts.noPackageData
        launchTask {
            val currentState = _state.value
            val app = currentState.apps.find { it.packageName == packageName }
            val risk = currentState.riskReports.find { it.packageName == packageName }
            runSuspendCatching {
                val content =
                    contentOverride ?: app?.let { buildPermissionsReport.single(it, risk, reportText).getOrThrow() }
                        ?: noData
                reportExporter.shareTextReport(
                    buildPermissionsReport.singleFileName(packageName).removeSuffix(".txt"),
                    "permissions",
                    content,
                ).getOrThrow()
            }
                .onSuccess { path ->
                    _state.update {
                        it.copy(
                            lastExportPath = path,
                            error = null,
                        )
                    }
                }
                .onFailure { error -> _state.update { it.copy(error = error.message) } }
        }
    }

    private fun requestReportPreview(packageName: String) {
        val currentState = _state.value
        val app = currentState.apps.find { it.packageName == packageName }
        val risk = currentState.riskReports.find { it.packageName == packageName }
        val reportText = useCaseTexts
        val reportCopy = presentationTexts
        _state.update {
            it.copy(
                reportPreviewPackage = packageName,
                reportPreviewLoading = true,
                reportPreviewContent = null,
                reportPreviewError = null,
            )
        }
        launchLatest(REPORT_PREVIEW_KEY) {
            val result = if (app == null) {
                Result.success(reportCopy.noPackageData)
            } else {
                buildPermissionsReport.single(app, risk, reportText)
            }
            ensureCurrent()
            result.onSuccess { content ->
                _state.update { it.copy(reportPreviewLoading = false, reportPreviewContent = content) }
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        reportPreviewLoading = false,
                        reportPreviewError = error.message ?: reportCopy.reportFailed,
                    )
                }
            }
        }
    }

    private fun dismissReportPreview() {
        cancelLatest(REPORT_PREVIEW_KEY)
        _state.update {
            it.copy(
                reportPreviewPackage = null,
                reportPreviewLoading = false,
                reportPreviewContent = null,
                reportPreviewError = null,
            )
        }
    }

    private fun startAutoRiskScan() {
        val apps = _state.value.apps
        if (apps.isEmpty()) {
            _state.update { it.copy(error = presentationTexts.noPermissionApps) }
            return
        }

        cancelRefresh()
        cancelAutoRiskScan()
        dismissReportPreview()
        updatePresentation {
            it.copy(
                error = null,
                loading = false,
                autoScanRunning = true,
                autoScanTotalApps = apps.size,
                autoScanScannedApps = 0,
                autoScanCurrentLabel = apps.firstOrNull()?.appLabel,
                showOnlyRisky = false,
                riskReports = emptyList(),
                findingsByPackage = emptyMap(),
                visibleFindingsByPackage = emptyMap(),
            )
        }

        launchLatest(RISK_SCAN_KEY) {
            val results = mutableListOf<PermissionRiskReport>()
            try {
                apps.forEachIndexed { index, app ->
                    ensureCurrent()
                    _state.update { it.copy(autoScanCurrentLabel = app.appLabel) }
                    val report = analyzePermissionRisks(app).getOrThrow()
                    ensureCurrent()
                    val sorted = if (report != null) {
                        results += report
                        withContext(dispatchers.default) {
                            analyzePermissionRisks.sortReports(results)
                        }
                    } else {
                        _state.value.riskReports
                    }
                    ensureCurrent()
                    updatePresentation {
                        it.copy(
                            autoScanScannedApps = index + 1,
                            riskReports = sorted,
                        )
                    }
                    delay(40)
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (failure: Exception) {
                if (isCurrent()) {
                    _state.update { it.copy(error = failure.message ?: presentationTexts.permissionScanFailed) }
                }
            } finally {
                if (isCurrent()) {
                    _state.update {
                        it.copy(
                            autoScanRunning = false,
                            autoScanCurrentLabel = null,
                        )
                    }
                }
            }
        }
    }

    private fun cancelAutoRiskScan() {
        cancelLatest(RISK_SCAN_KEY)
        _state.update {
            it.copy(
                autoScanRunning = false,
                autoScanCurrentLabel = null,
            )
        }
    }

    private fun cancelRefresh() {
        cancelLatest(REFRESH_KEY)
    }

    private fun normalizeQuery(value: String): String = value
        .trim()
        .replace(multiSpaceRegex, " ")
        .lowercase()

    private fun updatePresentation(
        languageChanged: Boolean = false,
        transform: (PermissionsState) -> PermissionsState,
    ) {
        _state.update { current ->
            val updated = transform(current)
            val risksChanged = updated.riskReports !== current.riskReports
            val findingsChanged = risksChanged || languageChanged
            val previousReports = if (risksChanged && !languageChanged) {
                current.riskReports.associateBy { it.packageName }
            } else {
                emptyMap()
            }
            val findings = if (findingsChanged) {
                updated.riskReports.associate { report ->
                    val items = if (!languageChanged && report === previousReports[report.packageName]) {
                        current.findingsByPackage.getValue(report.packageName)
                    } else {
                        report.findings.map { it.toUi(useCaseTexts) }
                    }
                    report.packageName to items
                }
            } else {
                current.findingsByPackage
            }
            val appsChanged = updated.apps !== current.apps ||
                risksChanged || updated.filter != current.filter ||
                updated.favorites !== current.favorites || updated.showOnlyRisky != current.showOnlyRisky
            updated.copy(
                visibleApps = if (appsChanged) visibleApps(updated) else current.visibleApps,
                favoriteQueries = if (updated.favorites !==
                    current.favorites
                ) {
                    updated.favorites.sorted()
                } else {
                    current.favoriteQueries
                },
                findingsByPackage = findings,
                visibleFindingsByPackage = if (findingsChanged) {
                    findings.mapValues { (_, values) -> values.filterNot { it.isPattern } }
                } else {
                    current.visibleFindingsByPackage
                },
            )
        }
    }
}
