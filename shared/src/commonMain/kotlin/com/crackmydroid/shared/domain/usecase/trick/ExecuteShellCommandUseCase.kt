package com.crackmydroid.shared.domain.usecase.trick

import com.crackmydroid.shared.domain.model.shell.ShellCommand
import com.crackmydroid.shared.domain.model.shell.ShellCommandResult
import com.crackmydroid.shared.domain.repository.ShellCommandRepository

class ExecuteShellCommandUseCase(private val repository: ShellCommandRepository) {
    suspend operator fun invoke(command: ShellCommand): ShellCommandResult =
        repository.execute(command)
}
