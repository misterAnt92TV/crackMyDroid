package com.crackmydroid.shared.data.securityadvisory

import com.crackmydroid.shared.domain.model.apkcontent.AdvisoryLookupAvailability
import com.crackmydroid.shared.domain.model.apkcontent.AdvisoryLookupFailureKind
import com.crackmydroid.shared.domain.model.apkcontent.VerifiedVulnerabilityLibrary
import io.ktor.http.HttpStatusCode
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

class SecurityAdvisoryRepositoryBackendTest {
    @Test
    fun `returns not configured without calling backend when endpoint is absent`() = runTest {
        val client = mockk<SecurityAdvisoryBackendClient>()
        val repository = SecurityAdvisoryRepositoryBackend(EndpointProvider(null), client)

        val report = repository.lookup(listOf(verifiedLibrary()))

        assertEquals(AdvisoryLookupAvailability.NotConfigured, report.availability)
    }

    @Test
    fun `maps backend failures to stable domain failure kinds`() = runTest {
        val cases = listOf(
            SecurityAdvisoryBackendFailure.Http(HttpStatusCode.TooManyRequests) to AdvisoryLookupFailureKind.RateLimited,
            SecurityAdvisoryBackendFailure.Http(HttpStatusCode.BadGateway) to AdvisoryLookupFailureKind.Network,
            SecurityAdvisoryBackendFailure.MalformedResponse(IllegalArgumentException()) to
                AdvisoryLookupFailureKind.InvalidResponse,
            SecurityAdvisoryBackendFailure.UnexpectedPackages to AdvisoryLookupFailureKind.InvalidResponse,
        )

        cases.forEach { (backendFailure, expectedFailure) ->
            val client = mockk<SecurityAdvisoryBackendClient>()
            coEvery { client.lookup(any(), any()) } throws backendFailure
            val repository = SecurityAdvisoryRepositoryBackend(EndpointProvider("https://security.example.test"), client)

            val report = repository.lookup(listOf(verifiedLibrary()))

            assertEquals(AdvisoryLookupAvailability.Available, report.availability)
            assertEquals(expectedFailure, report.failure)
        }
    }

    private fun verifiedLibrary() = VerifiedVulnerabilityLibrary(
        packageUrl = "pkg:maven/com.example/library@1.0.0",
        version = "1.0.0",
    )

    private class EndpointProvider(private val configuredEndpoint: String?) : SecurityAdvisoryEndpointProvider {
        override fun endpoint(): String? = configuredEndpoint
    }
}
