package com.crackmydroid.shared.domain.usecase

import com.crackmydroid.shared.domain.model.ShellCommand
import com.crackmydroid.shared.domain.model.ShellCommandResult
import com.crackmydroid.shared.domain.repository.ShellCommandRepository

class ListShellCommandsUseCase(private val repo: ShellCommandRepository) {
    suspend operator fun invoke(): List<ShellCommand> = repo.listCommands()
}

class ExecuteShellCommandUseCase(private val repo: ShellCommandRepository) {
    suspend operator fun invoke(command: ShellCommand): ShellCommandResult = repo.execute(command)
}
