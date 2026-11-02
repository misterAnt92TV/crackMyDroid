package com.crackmydroid.shared.domain.usecase

import com.crackmydroid.shared.domain.repository.SettingsRepository

class SetReduceMotionUseCase(private val repository: SettingsRepository) {
    suspend operator fun invoke(enabled: Boolean) = repository.setReduceMotion(enabled)
}
