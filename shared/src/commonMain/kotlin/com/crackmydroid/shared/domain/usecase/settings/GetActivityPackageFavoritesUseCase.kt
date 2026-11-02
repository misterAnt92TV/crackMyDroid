package com.crackmydroid.shared.domain.usecase

import com.crackmydroid.shared.domain.repository.SettingsRepository

class GetActivityPackageFavoritesUseCase(private val repository: SettingsRepository) {
    suspend operator fun invoke(): Set<String> = repository.getActivityPackageFavorites()
}
