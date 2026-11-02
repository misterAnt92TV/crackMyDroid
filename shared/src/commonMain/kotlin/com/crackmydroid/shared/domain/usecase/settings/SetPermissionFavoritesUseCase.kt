package com.crackmydroid.shared.domain.usecase

import com.crackmydroid.shared.domain.repository.SettingsRepository

class SetPermissionFavoritesUseCase(private val repository: SettingsRepository) {
    suspend operator fun invoke(values: Set<String>) = repository.setPermissionFavorites(values)
}
