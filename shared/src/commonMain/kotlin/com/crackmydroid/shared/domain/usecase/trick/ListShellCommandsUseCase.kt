package com.crackmydroid.shared.domain.usecase.trick

import co.touchlab.kermit.Logger
import com.crackmydroid.architecture.domain.usecase.BaseUseCase
import com.crackmydroid.architecture.logging.AppLogger
import com.crackmydroid.shared.domain.model.shell.ShellCommand
import com.crackmydroid.shared.domain.repository.ShellCommandRepository

class ListShellCommandsUseCase(private val repository: ShellCommandRepository, logger: Logger = AppLogger.logger) :
    BaseUseCase(TAG, logger = logger) {
    private companion object {
        private const val TAG = "ListShellCommandsUseCase"
    }

    suspend operator fun invoke(): Result<List<ShellCommand>> = execute { repository.listCommands() }
}
