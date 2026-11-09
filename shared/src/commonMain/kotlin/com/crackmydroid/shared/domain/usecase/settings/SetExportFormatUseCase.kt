package com.crackmydroid.shared.domain.usecase.settings

import com.crackmydroid.shared.domain.model.ExportFormat
import com.crackmydroid.shared.domain.repository.SettingsRepository
import co.touchlab.kermit.Logger
import com.crackmydroid.shared.AppLogger
import com.crackmydroid.shared.domain.usecase.BaseUseCase

class SetExportFormatUseCase(
    private val repository: SettingsRepository,
    logger: Logger = AppLogger.logger
) : BaseUseCase(TAG, logger = logger) {
    private companion object {
        private const val TAG = "SetExportFormatUseCase"
    }

    suspend operator fun invoke(format: ExportFormat): Result<Unit> = execute { repository.setExportFormat(format) }
}
