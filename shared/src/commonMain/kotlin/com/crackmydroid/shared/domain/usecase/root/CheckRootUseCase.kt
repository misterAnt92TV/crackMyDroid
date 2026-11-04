package com.crackmydroid.shared.domain.usecase.root

import com.crackmydroid.shared.domain.model.RootStatus
import com.crackmydroid.shared.domain.repository.RootCheckRepository
import com.crackmydroid.shared.domain.model.root.RootCheckMode

class CheckRootUseCase(private val repository: RootCheckRepository) {
    suspend operator fun invoke(mode: RootCheckMode = RootCheckMode.PASSIVE): RootStatus = repository.checkRoot(mode)
}
