package com.crackmydroid.shared.domain.usecase.settings

import com.crackmydroid.shared.domain.repository.SettingsRepository

class SetIntroSeenUseCase(private val repository: SettingsRepository) {
    suspend operator fun invoke(seen: Boolean) = repository.setIntroSeen(seen)
}
