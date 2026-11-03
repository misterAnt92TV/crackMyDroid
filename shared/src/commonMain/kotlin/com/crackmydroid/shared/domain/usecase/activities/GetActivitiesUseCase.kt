package com.crackmydroid.shared.domain.usecase.activities

import com.crackmydroid.shared.domain.model.ActivityEntry
import com.crackmydroid.shared.domain.repository.ActivityRepository

class GetActivitiesUseCase(private val repository: ActivityRepository) {
    suspend operator fun invoke(): List<ActivityEntry> = repository.getActivities()
}
