package com.crackmydroid.shared.domain.usecase.root

import com.crackmydroid.shared.domain.model.RootStatus
import com.crackmydroid.shared.domain.repository.RootCheckRepository

class CheckRootUseCase(private val repository: RootCheckRepository) {
    suspend operator fun invoke(): RootStatus = repository.checkRoot()
}
