package com.crackmydroid.shared.domain.usecase.root

import com.crackmydroid.shared.domain.model.PlayIntegrityResult
import com.crackmydroid.shared.domain.repository.RootCheckRepository

class CheckPlayIntegrityUseCase(private val repository: RootCheckRepository) {
    suspend operator fun invoke(nonce: String = "sample-nonce"): PlayIntegrityResult =
        repository.checkPlayIntegrity(nonce)
}
