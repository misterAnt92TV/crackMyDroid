package com.crackmydroid.shared.domain.usecase.root

import com.crackmydroid.shared.domain.model.PlayIntegrityResult
import com.crackmydroid.shared.domain.repository.IntegrityVerificationRepository

class VerifyPlayIntegrityTokenUseCase(private val repository: IntegrityVerificationRepository) {
    suspend operator fun invoke(token: String, requestBinding: String): PlayIntegrityResult =
        repository.verify(token, requestBinding)
}
