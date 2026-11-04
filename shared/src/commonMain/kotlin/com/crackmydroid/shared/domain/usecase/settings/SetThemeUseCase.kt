package com.crackmydroid.shared.domain.usecase.settings

import com.crackmydroid.shared.domain.model.app.AppTheme
import com.crackmydroid.shared.domain.repository.SettingsRepository

class SetThemeUseCase(private val repository: SettingsRepository) {
    suspend operator fun invoke(theme: AppTheme) = repository.setTheme(theme)
}
