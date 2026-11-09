package com.crackmydroid.shared.domain.usecase.settings

import co.touchlab.kermit.Logger
import com.crackmydroid.shared.AppLogger
import com.crackmydroid.shared.domain.model.app.AppTheme
import com.crackmydroid.shared.domain.repository.SettingsRepository
import com.crackmydroid.shared.domain.usecase.BaseUseCase

class SetThemeUseCase(
    private val repository: SettingsRepository,
    logger: Logger = AppLogger.logger,
) : BaseUseCase(TAG, logger = logger) {
    private companion object {
        private const val TAG = "SetThemeUseCase"
    }

    suspend operator fun invoke(theme: AppTheme): Result<Unit> = execute { repository.setTheme(theme) }
}
