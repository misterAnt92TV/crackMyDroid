package com.crackmydroid.shared.domain.usecase.trick

import co.touchlab.kermit.Logger
import com.crackmydroid.shared.AppLogger
import com.crackmydroid.shared.domain.model.shell.ShellCommand
import com.crackmydroid.shared.domain.repository.ShellCommandRepository
import com.crackmydroid.shared.domain.usecase.BaseUseCase

class ListShellCommandsUseCase(
    private val repository: ShellCommandRepository,
    logger: Logger = AppLogger.logger,
) : BaseUseCase(TAG, logger = logger) {
    private companion object {
        private const val TAG = "ListShellCommandsUseCase"
    }

    suspend operator fun invoke(): Result<List<ShellCommand>> = execute { repository.listCommands() }
}
