package com.crackmydroid.shared.domain.usecase

import com.crackmydroid.shared.domain.model.AppTheme
import com.crackmydroid.shared.domain.repository.SettingsRepository

class GetThemeUseCase(private val repository: SettingsRepository) {
    suspend operator fun invoke(): AppTheme = repository.getTheme()
}
