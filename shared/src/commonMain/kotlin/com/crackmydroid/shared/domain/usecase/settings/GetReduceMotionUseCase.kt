package com.crackmydroid.shared.domain.usecase

import com.crackmydroid.shared.domain.repository.SettingsRepository

class GetReduceMotionUseCase(private val repository: SettingsRepository) {
    suspend operator fun invoke(): Boolean = repository.isReduceMotion()
}
