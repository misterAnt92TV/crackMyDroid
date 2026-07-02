package com.crackmydroid.shared.domain.usecase

import com.crackmydroid.shared.domain.model.PlayIntegrityResult
import com.crackmydroid.shared.domain.model.RootStatus
import com.crackmydroid.shared.domain.repository.RootCheckRepository

class CheckRootUseCase(private val repo: RootCheckRepository) {
    suspend operator fun invoke(): RootStatus = repo.checkRoot()
}

class CheckPlayIntegrityUseCase(private val repo: RootCheckRepository) {
    suspend operator fun invoke(nonce: String = "sample-nonce"): PlayIntegrityResult = repo.checkPlayIntegrity(nonce)
}
