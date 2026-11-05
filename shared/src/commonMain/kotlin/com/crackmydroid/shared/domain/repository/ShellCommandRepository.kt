package com.crackmydroid.shared.domain.repository

import com.crackmydroid.shared.domain.model.shell.ShellCommand
import com.crackmydroid.shared.domain.model.shell.ShellCommandResult

interface ShellCommandRepository {
    suspend fun listCommands(): List<ShellCommand>
    suspend fun execute(command: ShellCommand): ShellCommandResult
}