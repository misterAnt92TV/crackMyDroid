package com.crackmydroid.shared.ui.info

import com.crackmydroid.shared.domain.usecase.info.BuildDeviceInfoReportUseCase
import com.crackmydroid.shared.domain.usecase.info.GetDeviceInfoUseCase
import com.crackmydroid.shared.domain.usecase.reports.ExportReportUseCase
import com.crackmydroid.shared.domain.usecase.root.CheckRootUseCase
import com.crackmydroid.shared.i18n.UseCasePresentationTexts
import com.crackmydroid.shared.i18n.UseCaseTexts
import com.crackmydroid.shared.presentation.BaseViewModel
import com.crackmydroid.shared.util.runSuspendCatching
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class InfoViewModel(
    private val getInfo: GetDeviceInfoUseCase,
    private val checkRoot: CheckRootUseCase,
    private val reportExporter: ExportReportUseCase,
    private val buildReport: BuildDeviceInfoReportUseCase,
) : BaseViewModel() {
    private val _state = MutableStateFlow(InfoState())
    val state: StateFlow<InfoState> = _state
    private var text = UseCaseTexts("en")
    private var reportJob: Job? = null
    private var reportRequest = 0L

    fun updateLanguage(languageCode: String) {
        val next = UseCaseTexts(languageCode)
        if (text.languageCode == next.languageCode) return
        text = next
        refreshReport()
    }

    fun load() {
        scope.launch {
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
        reportJob?.cancel()
        val request = ++reportRequest
        val info = _state.value.info ?: return
        val root = _state.value.root
        val reportText = text
        _state.update { it.copy(reportContent = null, reportLoading = true) }
        reportJob = scope.launch {
            val result = buildReport(info, root, reportText)
            if (request != reportRequest) return@launch
            result.onSuccess { content ->
                _state.update { it.copy(reportContent = content, reportLoading = false) }
            }.onFailure { error ->
                _state.update {
                    it.copy(
                        error = error.message ?: UseCasePresentationTexts(reportText.languageCode).reportFailed,
                        reportLoading = false,
                    )
                }
            }
        }
    }

    fun export() {
        val info = _state.value.info ?: return
        val root = _state.value.root
        val reportText = text
        scope.launch {
            runSuspendCatching {
                val content = buildReport(info, root, reportText).getOrThrow()
                reportExporter.saveTextReport("device_info", "device_info", content).getOrThrow()
            }.onSuccess { path ->
                _state.update { it.copy(lastExportPath = path, error = null) }
            }.onFailure { error -> _state.update { it.copy(error = error.message) } }
        }
    }

    fun share(contentOverride: String? = null) {
        val info = _state.value.info ?: return
        val root = _state.value.root
        val reportText = text
        scope.launch {
            runSuspendCatching {
                val content = contentOverride ?: buildReport(info, root, reportText).getOrThrow()
                reportExporter.shareTextReport("device_info", "device_info", content).getOrThrow()
            }.onSuccess { path ->
                _state.update { it.copy(lastExportPath = path, error = null) }
            }.onFailure { error -> _state.update { it.copy(error = error.message) } }
        }
    }
}
