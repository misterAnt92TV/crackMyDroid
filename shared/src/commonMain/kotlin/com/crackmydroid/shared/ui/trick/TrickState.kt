package com.crackmydroid.shared.ui.trick

import com.crackmydroid.architecture.presentation.model.UiState
import com.crackmydroid.architecture.presentation.model.UiLoadState
import com.crackmydroid.architecture.presentation.model.errorMessage
import com.crackmydroid.shared.domain.model.shell.ShellCommand
import com.crackmydroid.shared.domain.model.shell.ShellCommandResult

data class TrickState(
    val commands: List<ShellCommand> = emptyList(),
    val commandsLoadState: UiLoadState = UiLoadState.Idle,
    val executionLoadState: UiLoadState = UiLoadState.Idle,
    val lastResult: ShellCommandResult? = null,
    val lastCommandTitle: String? = null,
    val runningCommand: ShellCommand? = null,
    val pendingCommand: ShellCommand? = null,
    val resultToDisplay: ShellCommandResult? = null,
    val stdoutLines: List<String> = emptyList(),
    val stderrLines: List<String> = emptyList(),
    val packageLines: List<String> = emptyList(),
    val stdoutIsPackageList: Boolean = false,
    val interactionHint: String? = null,
) : UiState {
    val running: Boolean get() = executionLoadState is UiLoadState.Loading
    val errorMessage: String? get() = commandsLoadState.errorMessage ?: executionLoadState.errorMessage
}
