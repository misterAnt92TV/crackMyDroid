package com.crackmydroid.shared.data.root

import com.crackmydroid.shared.domain.model.PlayIntegrityResult
import com.crackmydroid.shared.domain.repository.IntegrityVerificationRepository

class IntegrityVerificationRepositoryDesktop : IntegrityVerificationRepository {
    override suspend fun verify(integrityToken: String, requestBinding: String): PlayIntegrityResult =
        PlayIntegrityResult(details = "La verifica Play Integrity richiede l'app Android sul device")
}
