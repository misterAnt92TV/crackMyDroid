package com.crackmydroid.shared.data.securityadvisory

import com.crackmydroid.shared.domain.model.apkcontent.AdvisoryLookupAvailability
import com.crackmydroid.shared.domain.model.apkcontent.AdvisoryLookupFailureKind
import com.crackmydroid.shared.domain.model.apkcontent.ApkLibraryVulnerabilityReport
import com.crackmydroid.shared.domain.model.apkcontent.VulnerabilityLookupLibrary
import com.crackmydroid.shared.domain.model.apkcontent.VerifiedVulnerabilityLibrary
import com.crackmydroid.shared.domain.repository.SecurityAdvisoryRepository
import kotlinx.coroutines.CancellationException

class SecurityAdvisoryRepositoryBackend(
    private val endpointProvider: SecurityAdvisoryEndpointProvider,
    private val backendClient: SecurityAdvisoryBackendClient,
) : SecurityAdvisoryRepository {
    override suspend fun lookup(libraries: List<VerifiedVulnerabilityLibrary>): ApkLibraryVulnerabilityReport {
        val endpoint = endpointProvider.endpoint() ?: return ApkLibraryVulnerabilityReport(
            availability = AdvisoryLookupAvailability.NotConfigured,
        )
        return try {
            ApkLibraryVulnerabilityReport(
                availability = AdvisoryLookupAvailability.Available,
                results = backendClient.lookup(endpoint, libraries),
            )
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (failure: SecurityAdvisoryBackendFailure) {
            ApkLibraryVulnerabilityReport(
                availability = AdvisoryLookupAvailability.Available,
                failure = failure.toDomainKind(),
            )
        }
    }

    private fun SecurityAdvisoryBackendFailure.toDomainKind() = when (this) {
        is SecurityAdvisoryBackendFailure.Http ->
            if (status.value == 429) AdvisoryLookupFailureKind.RateLimited else AdvisoryLookupFailureKind.Network
        is SecurityAdvisoryBackendFailure.MalformedResponse,
        SecurityAdvisoryBackendFailure.UnexpectedPackages,
        -> AdvisoryLookupFailureKind.InvalidResponse
    }
}
