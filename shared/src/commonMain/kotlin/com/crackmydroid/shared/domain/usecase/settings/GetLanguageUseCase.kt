package com.crackmydroid.shared.domain.usecase.settings


import com.crackmydroid.shared.domain.repository.SettingsRepository

class GetLanguageUseCase(private val repository: SettingsRepository) {
    suspend operator fun invoke() = repository.getLanguage()
}
