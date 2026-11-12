package com.crackmydroid.shared.ui.info

import com.crackmydroid.architecture.coroutines.AppDispatchers
import com.crackmydroid.shared.domain.usecase.info.BuildDeviceInfoReportUseCase
import com.crackmydroid.shared.domain.usecase.info.GetDeviceInfoUseCase
import com.crackmydroid.shared.domain.usecase.reports.ExportReportUseCase
import com.crackmydroid.shared.domain.usecase.root.CheckRootUseCase
import com.crackmydroid.shared.presentation.LocalizedBaseViewModel
import com.crackmydroid.shared.util.runSuspendCatching

class InfoViewModel(
    private val getInfoUseCase: GetDeviceInfoUseCase,
    private val checkRootUseCase: CheckRootUseCase,
    private val reportExporterUseCase: ExportReportUseCase,
    private val buildReportUseCase: BuildDeviceInfoReportUseCase,
    dispatchers: AppDispatchers,
) : LocalizedBaseViewModel<InfoEvent, InfoState>(InfoState(), dispatchers) {

    override fun onEvent(event: InfoEvent) = when (event) {
        is InfoEvent.Load -> load()
        is InfoEvent.Export -> export()
        is InfoEvent.Share -> share(event.contentOverride)
    }

    override fun onLanguageChanged() {
        refreshReport()
    }

    private fun load() {
        if (currentState.info != null && !currentState.loading) return
        launchTask {
            updateState { it.copy(loading = true, error = null) }
            runSuspendCatching {
                getInfoUseCase().getOrThrow() to checkRootUseCase().getOrThrow()
            }.onSuccess { (info, root) ->
                updateState { it.copy(info = info, root = root, loading = false) }
                refreshReport()
            }.onFailure { error ->
                updateState { it.copy(error = error.message, loading = false) }
            }
        }
    }

    private fun refreshReport() {
        val info = currentState.info ?: return
        val root = currentState.root
        val reportText = useCaseTexts
        val initialReportLoading = currentState.reportContent == null
        updateState { it.copy(reportLoading = initialReportLoading) }
        launchLatest("info-report") {
            val result = buildReportUseCase(info, root, reportText)
            if (!isCurrent()) return@launchLatest
            result.onSuccess { content ->
                updateState { it.copy(reportContent = content, reportLoading = false) }
            }.onFailure { error ->
                if (!isCurrent()) return@onFailure
                updateState {
                    it.copy(
                        error = error.message ?: presentationTexts.reportFailed,
                        reportLoading = false,
                    )
                }
            }
        }
    }

    private fun export() {
        val info = currentState.info ?: return
        val root = currentState.root
        val reportText = useCaseTexts
        launchTask {
            runSuspendCatching {
                val content = buildReportUseCase(info, root, reportText).getOrThrow()
                reportExporterUseCase.saveTextReport("device_info", "device_info", content).getOrThrow()
            }.onSuccess { path ->
                updateState { it.copy(lastExportPath = path, error = null) }
            }.onFailure { error -> updateState { it.copy(error = error.message) } }
        }
    }

    private fun share(contentOverride: String? = null) {
        val info = currentState.info ?: return
        val root = currentState.root
        val reportText = useCaseTexts
        launchTask {
            runSuspendCatching {
                val content = contentOverride ?: buildReportUseCase(info, root, reportText).getOrThrow()
                reportExporterUseCase.shareTextReport("device_info", "device_info", content).getOrThrow()
            }.onSuccess { path ->
                updateState { it.copy(lastExportPath = path, error = null) }
            }.onFailure { error -> updateState { it.copy(error = error.message) } }
        }
    }
}
