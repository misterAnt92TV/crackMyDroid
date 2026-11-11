package com.crackmydroid.shared.ui.info

import com.crackmydroid.architecture.coroutines.AppDispatchers
import com.crackmydroid.shared.domain.usecase.info.BuildDeviceInfoReportUseCase
import com.crackmydroid.shared.domain.usecase.info.GetDeviceInfoUseCase
import com.crackmydroid.shared.domain.usecase.reports.ExportReportUseCase
import com.crackmydroid.shared.domain.usecase.root.CheckRootUseCase
import com.crackmydroid.shared.presentation.LocalizedBaseViewModel
import com.crackmydroid.shared.util.runSuspendCatching
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class InfoViewModel(
    private val getInfo: GetDeviceInfoUseCase,
    private val checkRoot: CheckRootUseCase,
    private val reportExporter: ExportReportUseCase,
    private val buildReport: BuildDeviceInfoReportUseCase,
    dispatchers: AppDispatchers,
) : LocalizedBaseViewModel<InfoEvent>(dispatchers) {
    private val _state = MutableStateFlow(InfoState())
    val state: StateFlow<InfoState> = _state

    override fun onEvent(event: InfoEvent) = when (event) {
        is InfoEvent.Load -> load()
        is InfoEvent.Export -> export()
        is InfoEvent.Share -> share(event.contentOverride)
    }

    override fun onLanguageChanged() {
        refreshReport()
    }

    private fun load() {
        launchTask {
            _state.update { it.copy(loading = true, error = null) }
            runSuspendCatching {
                getInfo().getOrThrow() to checkRoot().getOrThrow()
            }.onSuccess { (info, root) ->
                _state.update { it.copy(info = info, root = root, loading = false) }
                refreshReport()
            }.onFailure { error ->
                _state.update { it.copy(error = error.message, loading = false) }
            }
        }
    }

    private fun refreshReport() {
        val info = _state.value.info ?: return
        val root = _state.value.root
        val reportText = useCaseTexts
        _state.update { it.copy(reportContent = null, reportLoading = true) }
        launchLatest("info-report") {
            val result = buildReport(info, root, reportText)
            if (!isCurrent()) return@launchLatest
            result.onSuccess { content ->
                _state.update { it.copy(reportContent = content, reportLoading = false) }
            }.onFailure { error ->
                if (!isCurrent()) return@onFailure
                _state.update {
                    it.copy(
                        error = error.message ?: presentationTexts.reportFailed,
                        reportLoading = false,
                    )
                }
            }
        }
    }

    private fun export() {
        val info = _state.value.info ?: return
        val root = _state.value.root
        val reportText = useCaseTexts
        launchTask {
            runSuspendCatching {
                val content = buildReport(info, root, reportText).getOrThrow()
                reportExporter.saveTextReport("device_info", "device_info", content).getOrThrow()
            }.onSuccess { path ->
                _state.update { it.copy(lastExportPath = path, error = null) }
            }.onFailure { error -> _state.update { it.copy(error = error.message) } }
        }
    }

    private fun share(contentOverride: String? = null) {
        val info = _state.value.info ?: return
        val root = _state.value.root
        val reportText = useCaseTexts
        launchTask {
            runSuspendCatching {
                val content = contentOverride ?: buildReport(info, root, reportText).getOrThrow()
                reportExporter.shareTextReport("device_info", "device_info", content).getOrThrow()
            }.onSuccess { path ->
                _state.update { it.copy(lastExportPath = path, error = null) }
            }.onFailure { error -> _state.update { it.copy(error = error.message) } }
        }
    }
}
