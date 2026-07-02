package com.crackmydroid.shared.domain.usecase

import com.crackmydroid.shared.domain.model.ActivityEntry
import com.crackmydroid.shared.domain.repository.ActivityRepository

class GetActivitiesUseCase(private val repo: ActivityRepository) {
    suspend operator fun invoke(): List<ActivityEntry> = repo.getActivities()
}

class LaunchActivityUseCase(private val repo: ActivityRepository) {
    suspend operator fun invoke(entry: ActivityEntry): Result<Unit> = repo.launch(entry)
}
