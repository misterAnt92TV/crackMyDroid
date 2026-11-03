package com.crackmydroid.shared.domain.usecase.trick

import com.crackmydroid.shared.domain.model.ShellCommand
import com.crackmydroid.shared.domain.repository.ShellCommandRepository

class ListShellCommandsUseCase(private val repository: ShellCommandRepository) {
    suspend operator fun invoke(): List<ShellCommand> = repository.listCommands()
}
