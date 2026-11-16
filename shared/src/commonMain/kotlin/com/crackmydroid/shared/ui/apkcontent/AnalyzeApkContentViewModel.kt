package com.crackmydroid.shared.ui.apkcontent

import com.crackmydroid.architecture.coroutines.AppDispatchers
import com.crackmydroid.architecture.presentation.model.UiLoadState
import com.crackmydroid.shared.domain.usecase.apkcontent.AnalyzeApkContentUseCase
import com.crackmydroid.shared.domain.usecase.apkcontent.DiscoverApkMavenLibrariesUseCase
import com.crackmydroid.shared.domain.usecase.apkcontent.LookupApkLibraryVulnerabilitiesUseCase
import com.crackmydroid.shared.domain.usecase.apkcontent.OpenAnalyzedApkArchiveUseCase
import com.crackmydroid.shared.domain.usecase.installedapps.ListInstalledAppsUseCase
import com.crackmydroid.shared.domain.usecase.reports.ExportReportUseCase
import com.crackmydroid.shared.domain.usecase.settings.GetAppFavoritesUseCase
import com.crackmydroid.shared.domain.usecase.settings.SetAppFavoritesUseCase
import com.crackmydroid.shared.domain.model.apkcontent.VulnerabilityLookupLibrary
import com.crackmydroid.shared.presentation.LocalizedBaseViewModel

class AnalyzeApkContentViewModel(
    private val listApps: ListInstalledAppsUseCase,
    private val analyzeApk: AnalyzeApkContentUseCase,
    private val discoverMavenLibraries: DiscoverApkMavenLibrariesUseCase,
    private val lookupLibraryVulnerabilities: LookupApkLibraryVulnerabilitiesUseCase,
    private val openArchive: OpenAnalyzedApkArchiveUseCase,
    private val reportExporter: ExportReportUseCase,
    private val getAppFavorites: GetAppFavoritesUseCase,
    private val setAppFavorites: SetAppFavoritesUseCase,
    dispatchers: AppDispatchers,
) : LocalizedBaseViewModel<AnalyzeApkContentEvent, AnalyzeApkContentState>(AnalyzeApkContentState(), dispatchers) {
    override fun onEvent(event: AnalyzeApkContentEvent) {
        when (event) {
            is AnalyzeApkContentEvent.Refresh -> refresh()

            is AnalyzeApkContentEvent.QueryChanged -> updateState { it.copy(query = event.query) }

            is AnalyzeApkContentEvent.FavoriteToggled -> toggleFavorite(event.packageName)

            is AnalyzeApkContentEvent.PackageSelected -> updateState {
                it.copy(
                    selectedPackage = event.packageName,
                    analysis = null,
                    mavenLibraries = emptyList(),
                    advisoryState = UiLoadState.Idle,
                    advisoryReport = null,
                    error = null,
                )
            }

            is AnalyzeApkContentEvent.ClearSelection -> updateState {
                it.copy(
                    selectedPackage = null,
                    analysis = null,
                    analysisState = UiLoadState.Idle,
                    mavenLibraries = emptyList(),
                    advisoryState = UiLoadState.Idle,
                    advisoryReport = null,
                    error = null,
                )
            }

            is AnalyzeApkContentEvent.AnalysisRequested -> analyze()

            is AnalyzeApkContentEvent.VulnerabilityLookupRequested -> lookupVulnerabilities()

            is AnalyzeApkContentEvent.ArchiveOpenRequested -> prepareAndOpenArchive()

            is AnalyzeApkContentEvent.ReportExportRequested -> export(false)

            is AnalyzeApkContentEvent.ReportShareRequested -> export(true)

            is AnalyzeApkContentEvent.ErrorDismissed -> updateState { it.copy(error = null) }

            is AnalyzeApkContentEvent.Reset -> updateState {
                it.copy(
                    selectedPackage = null,
                    analysis = null,
                    analysisState = UiLoadState.Idle,
                    mavenLibraries = emptyList(),
                    advisoryState = UiLoadState.Idle,
                    advisoryReport = null,
                    error = null,
                    exportPath = null,
                    query = "",
                )
            }
        }
    }
    private fun refresh() = launchLatest("apk-content-load") {
        getAppFavorites().onSuccess { favorites -> updateState { it.copy(favorites = favorites) } }
        updateState { it.copy(loadState = UiLoadState.Loading) }
        listApps().onSuccess { apps -> updateState { it.copy(apps = apps, loadState = UiLoadState.Success) } }
            .onFailure { error ->
                updateState {
                    it.copy(loadState = UiLoadState.Error(error.message ?: strings.commandError), error = error.message)
                }
            }
    }
    private fun toggleFavorite(packageName: String) {
        val favorites = currentState.favorites.let { if (packageName in it) it - packageName else it + packageName }
        updateState { it.copy(favorites = favorites) }
        launchTask { setAppFavorites(favorites).onFailure { error -> updateState { it.copy(error = error.message) } } }
    }
    private fun analyze() {
        val packageName = currentState.selectedPackage ?: return
        launchLatest("apk-content-analysis") {
            updateState {
                it.copy(
                    analysisState = UiLoadState.Loading,
                    error = null,
                    analysis = null,
                    mavenLibraries = emptyList(),
                    advisoryState = UiLoadState.Idle,
                    advisoryReport = null,
                )
            }
            analyzeApk(packageName).onSuccess { result ->
                updateState { it.copy(analysisState = UiLoadState.Success, analysis = result) }
                discoverMavenLibraries(packageName).onSuccess { discovery ->
                    updateState { it.copy(mavenLibraries = discovery.libraries) }
                }
            }
                .onFailure { error ->
                    updateState {
                        it.copy(
                            analysisState = UiLoadState.Error(error.message ?: strings.commandError),
                            error = error.message,
                        )
                    }
                }
        }
    }

    private fun lookupVulnerabilities() {
        val libraries = currentState.mavenLibraries
        if (libraries.isEmpty() || currentState.advisoryState is UiLoadState.Loading) return
        launchLatest("apk-content-advisory-lookup") {
            updateState { it.copy(advisoryState = UiLoadState.Loading, advisoryReport = null, error = null) }
            lookupLibraryVulnerabilities(
                libraries.map { library ->
                    VulnerabilityLookupLibrary(library.displayName, library.packageUrl, library.version)
                },
            ).onSuccess { report ->
                updateState { it.copy(advisoryState = UiLoadState.Success, advisoryReport = report) }
            }.onFailure { error ->
                updateState {
                    it.copy(
                        advisoryState = UiLoadState.Error(error.message ?: strings.commandError),
                        error = error.message ?: strings.commandError,
                    )
                }
            }
        }
    }
    private fun export(share: Boolean) {
        val analysis = currentState.analysis ?: return
        launchTask {
            val report = buildString {
                appendLine("Analyze APK Content")
                appendLine("Package: ${analysis.packageName}")
                appendLine("SHA-256: ${analysis.metadata.sha256}")
                appendLine("Entries: ${analysis.metadata.entryCount}")
                appendLine("Highest severity: ${analysis.highestSeverity}")
                appendLine("Libraries:")
                analysis.libraries.forEach { appendLine("- ${it.name}: ${it.evidence}") }
                if (currentState.mavenLibraries.isNotEmpty()) {
                    appendLine("Verifiable Maven libraries:")
                    currentState.mavenLibraries.forEach {
                        appendLine("- ${it.displayName}:${it.version} (${it.source})")
                    }
                }
                currentState.advisoryReport?.let { report ->
                    appendLine("Security advisories (${report.availability}):")
                    report.results.forEach { result ->
                        result.advisories.forEach { advisory ->
                            appendLine("- ${result.packageUrl}@${result.version}: ${advisory.advisoryId} [${advisory.severity}] ${advisory.summary}")
                        }
                    }
                }
                appendLine("Findings:")
                analysis.findings.forEach {
                    appendLine("- [${it.severity}] ${it.category}: ${it.evidence} — ${it.remediation}")
                }
                if (analysis.plaintextContent.items.isNotEmpty()) {
                    appendLine("Readable / Plaintext Content (${analysis.plaintextContent.totalCount} items):")
                    analysis.plaintextContent.items.forEach {
                        appendLine("- [${it.category}] (${it.source}) ${it.value}")
                    }
                }
                appendLine("Offline static analysis only; this is not an antivirus verdict.")
            }
            val result = if (share) {
                reportExporter.shareTextReport("apk_content_${analysis.packageName}", "apk_content", report)
            } else {
                reportExporter.saveTextReport("apk_content_${analysis.packageName}", "apk_content", report)
            }
            result.onSuccess { path -> updateState { it.copy(exportPath = path) } }.onFailure { error ->
                updateState {
                    it.copy(
                        error =
                            error.message ?: strings.commandError,
                    )
                }
            }
        }
    }

    private fun prepareAndOpenArchive() {
        val packageName = currentState.analysis?.packageName ?: return
        if (currentState.archiveNavigationState is UiLoadState.Loading) return
        launchLatest("apk-content-archive-navigation") {
            updateState { it.copy(archiveNavigationState = UiLoadState.Loading, error = null) }
            openArchive(packageName).onFailure { error ->
                updateState {
                    it.copy(
                        archiveNavigationState = UiLoadState.Error(error.message ?: strings.commandError),
                        error = error.message ?: strings.commandError,
                    )
                }
            }.onSuccess {
                updateState { it.copy(archiveNavigationState = UiLoadState.Success) }
            }
        }
    }
}
