package com.crackmydroid.shared.domain.usecase.activities

import co.touchlab.kermit.Logger
import com.crackmydroid.shared.AppLogger
import com.crackmydroid.shared.domain.model.activity.ActivityEntry
import com.crackmydroid.shared.domain.repository.ActivityRepository
import com.crackmydroid.shared.domain.usecase.BaseUseCase

class GetActivitiesUseCase(
    private val repository: ActivityRepository,
    logger: Logger = AppLogger.logger,
) : BaseUseCase(TAG, logger = logger) {
    private companion object {
        private const val TAG = "GetActivitiesUseCase"
    }

    suspend operator fun invoke(): Result<List<ActivityEntry>> = execute { repository.getActivities() }
}
