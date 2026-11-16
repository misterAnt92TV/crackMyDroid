package com.crackmydroid.shared.domain.usecase.settings

import co.touchlab.kermit.Logger
import com.crackmydroid.architecture.domain.usecase.BaseUseCase
import com.crackmydroid.architecture.logging.AppLogger
import com.crackmydroid.shared.domain.repository.SettingsRepository

class GetAppFavoritesUseCase(private val repository: SettingsRepository, logger: Logger = AppLogger.logger) :
    BaseUseCase(TAG, logger = logger) {
    private companion object { private const val TAG = "GetAppFavoritesUseCase" }
    suspend operator fun invoke(): Result<Set<String>> = execute { repository.getAppFavorites() }
}
