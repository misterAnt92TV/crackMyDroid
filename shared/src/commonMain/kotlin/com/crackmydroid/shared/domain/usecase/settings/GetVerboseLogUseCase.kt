package com.crackmydroid.shared.domain.usecase

import com.crackmydroid.shared.domain.repository.SettingsRepository

class GetVerboseLogUseCase(private val repository: SettingsRepository) {
    suspend operator fun invoke(): Boolean = repository.isVerboseLog()
}
