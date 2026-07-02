package com.crackmydroid.shared.ui.root

import com.crackmydroid.shared.domain.model.RootStatus
import com.crackmydroid.shared.domain.usecase.CheckRootUseCase
import com.crackmydroid.shared.i18n.Strings
import com.crackmydroid.shared.presentation.BaseViewModel
import com.crackmydroid.shared.util.saveTextFile
import com.crackmydroid.shared.util.shareTextFile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RootViewModel(
    private val checkRoot: CheckRootUseCase,
    private val checkPlayIntegrity: com.crackmydroid.shared.domain.usecase.CheckPlayIntegrityUseCase
) : BaseViewModel() {
    private val _state = MutableStateFlow(RootState())
    val state: StateFlow<RootState> = _state

    private fun buildReportContent(strings: Strings): String = buildString {
        appendLine(strings.navRoot)
        appendLine("-----------")
        _state.value.status?.let { status ->
            appendLine("${strings.verifyRoot}: ${if (status.isRooted) strings.rootDetected else strings.rootNotDetected}")
            appendLine("Dettagli root: ${status.details}")
        } ?: appendLine("${strings.verifyRoot}: n/d")
        _state.value.playIntegrity?.let { pi ->
            appendLine()
            appendLine(strings.playIntegrityTitle)
            appendLine("${strings.playIntegrityBasic}: ${pi.basicIntegrity ?: "n/d"}")
            appendLine("${strings.playIntegrityDevice}: ${pi.deviceIntegrity ?: "n/d"}")
            appendLine("${strings.playIntegrityDetails}: ${pi.details}")
        }
    }

    fun verify() {
        scope.launch {
            _state.update { it.copy(loading = true, error = null) }
            runCatching { checkRoot() }
                .onSuccess { status -> _state.update { it.copy(status = status, loading = false) } }
                .onFailure { err -> _state.update { it.copy(error = err.message, loading = false) } }
        }
    }

    fun verifyPlayIntegrity(nonce: String = "sample-nonce") {
        scope.launch {
            _state.update { it.copy(loading = true, error = null) }
            runCatching { checkPlayIntegrity(nonce) }
                .onSuccess { res -> _state.update { it.copy(playIntegrity = res, loading = false) } }
                .onFailure { err -> _state.update { it.copy(error = err.message, loading = false) } }
        }
    }

    fun shareReport(strings: Strings) {
        val content = buildReportContent(strings)
        val res = saveTextFile("root_test_report", content)
        res.onSuccess { path ->
            _state.update { it.copy(reportPath = path) }
            shareTextFile(path).onFailure { err ->
                _state.update { it.copy(error = err.message ?: "Errore") }
            }
        }.onFailure { err ->
            _state.update { it.copy(error = err.message ?: "Errore") }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}
