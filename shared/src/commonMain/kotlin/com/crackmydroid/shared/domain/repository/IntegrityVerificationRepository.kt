package com.crackmydroid.shared.domain.repository

import com.crackmydroid.shared.domain.model.PlayIntegrityResult

/**
 * Boundary to the product backend. The backend owns Google credentials and decodes the token.
 */
interface IntegrityVerificationRepository {
    suspend fun verify(integrityToken: String, requestBinding: String): PlayIntegrityResult
}
