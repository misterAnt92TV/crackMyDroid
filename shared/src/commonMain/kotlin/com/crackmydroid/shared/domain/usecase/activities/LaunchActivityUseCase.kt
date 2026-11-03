package com.crackmydroid.shared.domain.usecase.activities

import com.crackmydroid.shared.domain.model.ActivityEntry
import com.crackmydroid.shared.domain.repository.ActivityRepository

class LaunchActivityUseCase(private val repository: ActivityRepository) {
    suspend operator fun invoke(
        entry: ActivityEntry,
        action: String? = null,
        dataUri: String? = null,
        mimeType: String? = null
    ): Result<Unit> = repository.launch(entry, action, dataUri, mimeType)
}
