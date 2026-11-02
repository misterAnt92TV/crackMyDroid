package com.crackmydroid.shared.domain.usecase

import com.crackmydroid.shared.domain.repository.SettingsRepository

class SetActivityFavoritesUseCase(private val repository: SettingsRepository) {
    suspend operator fun invoke(values: Set<String>) = repository.setActivityFavorites(values)
}
