package com.crackmydroid.shared.domain.usecase.settings

import com.crackmydroid.shared.domain.repository.SettingsRepository
import co.touchlab.kermit.Logger
import com.crackmydroid.shared.AppLogger
import com.crackmydroid.shared.domain.usecase.BaseUseCase

class GetVerboseLogUseCase(
    private val repository: SettingsRepository,
    logger: Logger = AppLogger.logger
) : BaseUseCase(TAG, logger = logger) {
    private companion object {
        private const val TAG = "GetVerboseLogUseCase"
    }

    suspend operator fun invoke(): Result<Boolean> = execute { repository.isVerboseLog() }
}
