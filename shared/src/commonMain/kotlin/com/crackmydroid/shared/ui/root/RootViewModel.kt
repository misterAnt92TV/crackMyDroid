package com.crackmydroid.shared.ui.root

import com.crackmydroid.shared.domain.usecase.root.CheckRootUseCase
import com.crackmydroid.shared.domain.model.root.RootCheckMode
import com.crackmydroid.shared.domain.usecase.reports.ExportReportUseCase
import com.crackmydroid.shared.domain.usecase.root.CheckPlayIntegrityUseCase
import com.crackmydroid.shared.i18n.Strings
import com.crackmydroid.shared.presentation.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RootViewModel(
    private val checkRoot: CheckRootUseCase,
    private val checkPlayIntegrity: CheckPlayIntegrityUseCase,
    private val reportExporter: ExportReportUseCase
) : BaseViewModel() {
    private val _state = MutableStateFlow(RootState())
    val state: StateFlow<RootState> = _state

    private fun buildReportContent(strings: Strings): String = buildString {
        appendLine(strings.navRoot)
        appendLine("-----------")
        _state.value.status?.let { status ->
            appendLine("${strings.verifyRoot}: ${if (status.isRooted) strings.rootDetected else strings.rootNotDetected}")
            appendLine("Dettagli root: ${status.details}")
        } ?: appendLine("${strings.verifyRoot}: ${strings.notAvailable}")
        _state.value.playIntegrity?.let { pi ->
            appendLine()
            appendLine(strings.playIntegrityTitle)
            appendLine("${strings.playIntegrityBasic}: ${pi.basicIntegrity ?: strings.notAvailable}")
            appendLine("${strings.playIntegrityDevice}: ${pi.deviceIntegrity ?: strings.notAvailable}")
            appendLine("${strings.playIntegrityDetails}: ${pi.details}")
        }
    }

    fun verify() {
        verify(RootCheckMode.PASSIVE)
    }

    fun verifyRootAccess() {
        verify(RootCheckMode.PRIVILEGE_ACCESS)
    }

    private fun verify(mode: RootCheckMode) {
        scope.launch {
            _state.update { it.copy(loading = true, error = null) }
            runCatching { checkRoot(mode) }
                .onSuccess { status -> _state.update { it.copy(status = status, loading = false) } }
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

    fun updatePlayIntegrityNonce(value: String) {
        _state.update { it.copy(playIntegrityNonce = value) }
    }

    fun verifyPlayIntegrity() {
        val nonce = _state.value.playIntegrityNonce.ifBlank { "sample-nonce" }
        scope.launch {
            _state.update { it.copy(loading = true, error = null) }
            runCatching { checkPlayIntegrity(nonce) }
                .onSuccess { res ->
                    _state.update {
                        it.copy(
                            playIntegrity = res,
                            loading = false
                        )
                    }
                }
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

    fun shareReport(strings: Strings) {
        val content = buildReportContent(strings)
        scope.launch {
            reportExporter.share("root_test_report", content)
                .onSuccess { path -> _state.update { it.copy(reportPath = path, error = null) } }
                .onFailure { error -> _state.update { it.copy(error = error.message ?: "Errore") } }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}
