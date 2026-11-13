package com.crackmydroid.shared.domain.usecase.activities

import co.touchlab.kermit.Logger
import com.crackmydroid.architecture.domain.usecase.BaseUseCase
import com.crackmydroid.architecture.logging.AppLogger
import com.crackmydroid.shared.domain.model.activity.ActivityEntry
import com.crackmydroid.shared.domain.repository.ActivityRepository

class LaunchActivityUseCase(private val repository: ActivityRepository, logger: Logger = AppLogger.logger) :
    BaseUseCase(TAG, logger = logger) {
    private companion object {
        private const val TAG = "LaunchActivityUseCase"
    }

    suspend operator fun invoke(
        entry: ActivityEntry,
        action: String? = null,
        dataUri: String? = null,
        mimeType: String? = null,
    ): Result<Unit> = execute {
        repository.launch(entry, action, dataUri, mimeType).getOrThrow()
    }
}
