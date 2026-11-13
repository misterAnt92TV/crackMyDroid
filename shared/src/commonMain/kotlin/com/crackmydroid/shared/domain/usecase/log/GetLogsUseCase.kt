package com.crackmydroid.shared.domain.usecase.log

import co.touchlab.kermit.Logger
import com.crackmydroid.architecture.domain.usecase.BaseUseCase
import com.crackmydroid.architecture.logging.AppLogger
import com.crackmydroid.shared.domain.repository.log.LogRepository

class GetLogsUseCase(private val repository: LogRepository, logger: Logger = AppLogger.logger) :
    BaseUseCase(TAG, logger = logger) {
    private companion object {
        private const val TAG = "GetLogsUseCase"
    }

    suspend operator fun invoke(): Result<List<String>> = execute { repository.fetchLog() }
}
