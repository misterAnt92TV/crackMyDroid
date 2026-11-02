package com.crackmydroid.shared.domain.usecase

import com.crackmydroid.shared.domain.model.ShellCommand
import com.crackmydroid.shared.domain.model.ShellCommandResult
import com.crackmydroid.shared.domain.repository.ShellCommandRepository

class ExecuteShellCommandUseCase(private val repository: ShellCommandRepository) {
    suspend operator fun invoke(command: ShellCommand): ShellCommandResult = repository.execute(command)
}
