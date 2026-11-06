package com.crackmydroid.shared.domain.usecase.trick

import com.crackmydroid.shared.domain.model.shell.ShellCommand
import com.crackmydroid.shared.domain.model.shell.ShellCommandResult
import com.crackmydroid.shared.domain.repository.ShellCommandRepository
import co.touchlab.kermit.Logger
import com.crackmydroid.shared.AppLogger
import com.crackmydroid.shared.domain.usecase.BaseUseCase

class ExecuteShellCommandUseCase(
    private val repository: ShellCommandRepository,
    logger: Logger = AppLogger.logger
) : BaseUseCase(TAG, logger = logger) {
    private companion object {
        private const val TAG = "ExecuteShellCommandUseCase"
    }

    suspend operator fun invoke(command: ShellCommand): Result<ShellCommandResult> =
        execute { repository.execute(command) }
}
