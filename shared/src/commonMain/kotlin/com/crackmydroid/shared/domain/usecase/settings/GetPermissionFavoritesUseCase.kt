package com.crackmydroid.shared.domain.usecase.settings


import com.crackmydroid.shared.domain.repository.SettingsRepository

class GetPermissionFavoritesUseCase(private val repository: SettingsRepository) {
    suspend operator fun invoke(): Set<String> = repository.getPermissionFavorites()
}
