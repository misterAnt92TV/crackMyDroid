package com.crackmydroid.shared.domain.usecase

import com.crackmydroid.shared.domain.model.AppLanguage
import com.crackmydroid.shared.domain.repository.SettingsRepository

class SetLanguageUseCase(private val repository: SettingsRepository) {
    suspend operator fun invoke(language: AppLanguage) = repository.setLanguage(language)
}
