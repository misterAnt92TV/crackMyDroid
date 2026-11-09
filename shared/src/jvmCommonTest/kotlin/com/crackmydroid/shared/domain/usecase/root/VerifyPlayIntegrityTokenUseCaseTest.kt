package com.crackmydroid.shared.domain.usecase.root

import com.crackmydroid.shared.domain.model.PlayIntegrityResult
import com.crackmydroid.shared.domain.repository.IntegrityVerificationRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class VerifyPlayIntegrityTokenUseCaseTest {
    @Test
    fun forwardsOnlyTokenAndRequestBindingToBackendBoundary() = runTest {
        val backend = mockk<IntegrityVerificationRepository>()
        val expected = PlayIntegrityResult(
            details = "verified",
            appRecognitionVerdict = "PLAY_RECOGNIZED",
            appLicensingVerdict = "LICENSED"
        )
        coEvery { backend.verify("encrypted-token", "scan-nonce") } returns expected

        val result = VerifyPlayIntegrityTokenUseCase(backend)("encrypted-token", "scan-nonce")

        assertEquals(expected, result.getOrThrow())
        coVerify(exactly = 1) { backend.verify("encrypted-token", "scan-nonce") }
    }
}
