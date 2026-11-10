package com.crackmydroid.shared.domain.usecase.settings

import co.touchlab.kermit.Logger
import com.crackmydroid.architecture.domain.usecase.BaseUseCase
import com.crackmydroid.architecture.logging.AppLogger
import com.crackmydroid.shared.domain.model.app.AppLanguage
import com.crackmydroid.shared.domain.repository.SettingsRepository

class GetLanguageUseCase(private val repository: SettingsRepository, logger: Logger = AppLogger.logger) :
    BaseUseCase(TAG, logger = logger) {
    private companion object {
        private const val TAG = "GetLanguageUseCase"
    }

    suspend operator fun invoke(): Result<AppLanguage> = execute { repository.getLanguage() }
}
