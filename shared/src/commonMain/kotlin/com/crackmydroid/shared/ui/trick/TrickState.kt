package com.crackmydroid.shared.ui.trick

import com.crackmydroid.shared.domain.model.shell.ShellCommand
import com.crackmydroid.shared.domain.model.shell.ShellCommandResult

data class TrickState(
    val commands: List<ShellCommand> = emptyList(),
    val lastResult: ShellCommandResult? = null,
    val lastCommandTitle: String? = null,
    val runningCommand: ShellCommand? = null,
    val pendingCommand: ShellCommand? = null,
    val resultToDisplay: ShellCommandResult? = null,
    val stdoutLines: List<String> = emptyList(),
    val stderrLines: List<String> = emptyList(),
    val packageLines: List<String> = emptyList(),
    val stdoutIsPackageList: Boolean = false,
    val running: Boolean = false,
    val interactionHint: String? = null,
    val error: String? = null,
)
