package com.crackmydroid.shared.ui.trick

import com.crackmydroid.shared.domain.model.ShellCommand
import com.crackmydroid.shared.domain.model.ShellCommandResult

data class TrickState(
    val commands: List<ShellCommand> = emptyList(),
    val lastResult: ShellCommandResult? = null,
    val lastCommandTitle: String? = null,
    val runningCommand: ShellCommand? = null,
    val running: Boolean = false,
    val interactionHint: String? = null,
    val error: String? = null
)
