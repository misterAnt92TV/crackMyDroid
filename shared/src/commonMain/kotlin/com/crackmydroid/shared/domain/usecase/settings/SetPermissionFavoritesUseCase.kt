package com.crackmydroid.shared.domain.usecase.settings

import co.touchlab.kermit.Logger
import com.crackmydroid.architecture.domain.usecase.BaseUseCase
import com.crackmydroid.architecture.logging.AppLogger
import com.crackmydroid.shared.domain.repository.SettingsRepository

class SetPermissionFavoritesUseCase(private val repository: SettingsRepository, logger: Logger = AppLogger.logger) :
    BaseUseCase(TAG, logger = logger) {
    private companion object {
        private const val TAG = "SetPermissionFavoritesUseCase"
    }

    suspend operator fun invoke(values: Set<String>): Result<Unit> =
        execute { repository.setPermissionFavorites(values) }
}
