package com.crackmydroid.shared.ui.permissions

import com.crackmydroid.shared.domain.model.AppPermissionEntry
import com.crackmydroid.shared.domain.model.PermissionRiskReport
import com.crackmydroid.shared.domain.usecase.ListPermissionsUseCase
import com.crackmydroid.shared.domain.usecase.permissions.AnalyzePermissionRisksUseCase
import com.crackmydroid.shared.domain.usecase.permissions.BuildPermissionsReportUseCase
import com.crackmydroid.shared.presentation.BaseViewModel
import com.crackmydroid.shared.util.saveTextFile
import com.crackmydroid.shared.util.shareTextFile
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PermissionsViewModel(
    private val listPermissions: ListPermissionsUseCase,
    private val analyzePermissionRisks: AnalyzePermissionRisksUseCase,
    private val buildPermissionsReport: BuildPermissionsReportUseCase
) : BaseViewModel() {
    private val _state = MutableStateFlow(PermissionsState())
    val state: StateFlow<PermissionsState> = _state
    private val queryCount = mutableMapOf<String, Int>()
    private var riskScanJob: Job? = null
    private val multiSpaceRegex = Regex("\\s+")

    fun refresh() {
        cancelAutoRiskScan()
        scope.launch {
            _state.update {
                it.copy(
                    loading = true,
                    error = null,
                    autoScanRunning = false,
                    autoScanTotalApps = 0,
                    autoScanScannedApps = 0,
                    autoScanCurrentLabel = null,
                    showOnlyRisky = false,
                    riskReports = emptyList()
                )
            }
            runCatching { listPermissions() }
                .onSuccess { apps -> _state.update { it.copy(apps = apps, loading = false) } }
                .onFailure { err -> _state.update { it.copy(error = err.message, loading = false) } }
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

    fun setShowOnlyRisky(enabled: Boolean) {
        _state.update { it.copy(showOnlyRisky = enabled) }
    }

    fun toggleShowOnlyRisky() {
        _state.update { it.copy(showOnlyRisky = !it.showOnlyRisky) }
    }

    fun filtered(): List<AppPermissionEntry> {
        val state = _state.value
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
        val currentState = _state.value
        val content = buildPermissionsReport(currentState.apps, currentState.riskReports)
        val result = saveTextFile("permissions_report.txt", content)
        result.onSuccess { path -> _state.update { it.copy(lastExportPath = path, error = null) } }
            .onFailure { err -> _state.update { it.copy(error = err.message) } }
    }

    fun toggleFavorite(packageName: String) {
        _state.update {
            val favorites = it.favorites.toMutableSet()
            if (favorites.contains(packageName)) favorites.remove(packageName) else favorites.add(packageName)
            it.copy(favorites = favorites)
        }
    }

    fun shareSingle(packageName: String): Result<String> {
        val currentState = _state.value
        val app = currentState.apps.find { it.packageName == packageName }
        val risk = currentState.riskReports.find { it.packageName == packageName }
        val content = app?.let { buildPermissionsReport.single(it, risk) }
            ?: "Nessun dato disponibile per il package selezionato."

        val result = saveTextFile(buildPermissionsReport.singleFileName(packageName), content)
        result.onFailure { err -> _state.update { it.copy(error = err.message) } }
        result.onSuccess { path ->
            _state.update { it.copy(lastExportPath = path, error = null) }
            shareTextFile(path).onFailure { err -> _state.update { it.copy(error = err.message) } }
        }
        return result
    }

    fun singleReportPreview(packageName: String): String {
        val currentState = _state.value
        val app = currentState.apps.find { it.packageName == packageName }
        val risk = currentState.riskReports.find { it.packageName == packageName }
        return app?.let { buildPermissionsReport.single(it, risk) }
            ?: "Nessun dato disponibile per il package selezionato."
    }

    fun startAutoRiskScan() {
        val apps = _state.value.apps
        if (apps.isEmpty()) {
            _state.update { it.copy(error = "Nessuna app disponibile. Aggiorna prima la lista permessi.") }
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
                riskReports = emptyList()
            )
        }

        riskScanJob = scope.launch {
            val results = mutableListOf<PermissionRiskReport>()
            try {
                apps.forEachIndexed { index, app ->
                    _state.update { it.copy(autoScanCurrentLabel = app.appLabel) }
                    analyzePermissionRisks(app)?.let { results += it }
                    _state.update {
                        it.copy(
                            autoScanScannedApps = index + 1,
                            riskReports = analyzePermissionRisks.sortReports(results)
                        )
                    }
                    delay(40)
                }
            } catch (_: CancellationException) {
                // user interrupted scan
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
}
