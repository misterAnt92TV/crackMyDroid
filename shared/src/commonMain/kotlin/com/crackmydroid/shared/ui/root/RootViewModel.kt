package com.crackmydroid.shared.ui.root

import com.crackmydroid.architecture.coroutines.AppDispatchers
import com.crackmydroid.shared.domain.model.root.RootCheckMode
import com.crackmydroid.shared.domain.usecase.reports.ExportReportUseCase
import com.crackmydroid.shared.domain.usecase.root.CheckPlayIntegrityUseCase
import com.crackmydroid.shared.domain.usecase.root.CheckRootUseCase
import com.crackmydroid.shared.presentation.LocalizedBaseViewModel

class RootViewModel(
    private val checkRootUseCase: CheckRootUseCase,
    private val checkPlayIntegrityUseCase: CheckPlayIntegrityUseCase,
    private val reportExporterUseCase: ExportReportUseCase,
    dispatchers: AppDispatchers,
) : LocalizedBaseViewModel<RootEvent, RootState>(RootState(), dispatchers) {

    override fun onEvent(event: RootEvent) = when (event) {
        is RootEvent.Verify -> verify()
        is RootEvent.VerifyRootAccess -> verifyRootAccess()
        is RootEvent.PlayIntegrityNonceChanged -> updatePlayIntegrityNonce(event.value)
        is RootEvent.VerifyPlayIntegrity -> verifyPlayIntegrity()
        is RootEvent.ShareReport -> shareReport()
        is RootEvent.ErrorDismissed -> clearError()
    }

    private fun buildReportContent(): String = buildString {
        appendLine(strings.navRoot)
        appendLine("-----------")
        currentState.status?.let { status ->
            appendLine(
                "${strings.verifyRoot}: ${if (status.isRooted) strings.rootDetected else strings.rootNotDetected}",
            )
            appendLine("${strings.rootDetectedDetails}: ${status.details}")
        } ?: appendLine("${strings.verifyRoot}: ${strings.notAvailable}")
        currentState.playIntegrity?.let { pi ->
            appendLine()
            appendLine(strings.playIntegrityTitle)
            appendLine("${strings.playIntegrityBasic}: ${pi.basicIntegrity ?: strings.notAvailable}")
            appendLine("${strings.playIntegrityDevice}: ${pi.deviceIntegrity ?: strings.notAvailable}")
            appendLine("${strings.playIntegrityDetails}: ${pi.details}")
        }
    }

    private fun verify() {
        verify(RootCheckMode.PASSIVE)
    }

    private fun verifyRootAccess() {
        verify(RootCheckMode.PRIVILEGE_ACCESS)
    }

    private fun verify(mode: RootCheckMode) {
        launchLatest("root-check") {
            updateState { it.copy(loading = true, error = null) }
            checkRootUseCase(mode)
                .onSuccess { status ->
                    if (isCurrent()) updateState { it.copy(status = status, loading = false) }
                }
                .onFailure { err ->
                    if (!isCurrent()) return@onFailure
                    updateState {
                        it.copy(
                            error = err.message,
                            loading = false,
                        )
                    }
                }
        }
    }

    private fun updatePlayIntegrityNonce(value: String) {
        updateState { it.copy(playIntegrityNonce = value) }
    }

    private fun verifyPlayIntegrity() {
        val nonce = currentState.playIntegrityNonce.ifBlank { "sample-nonce" }
        launchTask {
            updateState { it.copy(loading = true, error = null) }
            checkPlayIntegrityUseCase(nonce)
                .onSuccess { res ->
                    updateState {
                        it.copy(
                            playIntegrity = res,
                            loading = false,
                        )
                    }
                }
                .onFailure { err ->
                    updateState {
                        it.copy(
                            error = err.message,
                            loading = false,
                        )
                    }
                }
        }
    }

    private fun shareReport() {
        val content = buildReportContent()
        launchTask {
            reportExporterUseCase.shareTextReport("root_test_report", "root_test", content)
                .onSuccess { path -> updateState { it.copy(reportPath = path, error = null) } }
                .onFailure { error -> updateState { it.copy(error = error.message ?: presentationTexts.error) } }
        }
    }

    private fun clearError() {
        updateState { it.copy(error = null) }
    }
}
