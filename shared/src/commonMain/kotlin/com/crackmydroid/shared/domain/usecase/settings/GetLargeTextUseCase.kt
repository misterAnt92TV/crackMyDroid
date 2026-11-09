package com.crackmydroid.shared.domain.usecase.settings


import co.touchlab.kermit.Logger
import com.crackmydroid.shared.AppLogger
import com.crackmydroid.shared.domain.repository.SettingsRepository
import com.crackmydroid.shared.domain.usecase.BaseUseCase

class GetLargeTextUseCase(
    private val repository: SettingsRepository,
    logger: Logger = AppLogger.logger,
) : BaseUseCase(TAG, logger = logger) {
    private companion object {
        private const val TAG = "GetLargeTextUseCase"
    }

    suspend operator fun invoke(): Result<Boolean> = execute { repository.isLargeText() }
}
