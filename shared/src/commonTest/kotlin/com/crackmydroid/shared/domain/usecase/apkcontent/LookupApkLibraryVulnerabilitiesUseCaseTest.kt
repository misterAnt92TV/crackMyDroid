package com.crackmydroid.shared.domain.usecase.apkcontent

import com.crackmydroid.shared.domain.model.apkcontent.AdvisoryLookupAvailability
import com.crackmydroid.shared.domain.model.apkcontent.ApkLibraryVulnerabilityReport
import com.crackmydroid.shared.domain.model.apkcontent.UnverifiedLibraryReason
import com.crackmydroid.shared.domain.model.apkcontent.VulnerabilityLookupLibrary
import com.crackmydroid.shared.domain.model.apkcontent.VerifiedVulnerabilityLibrary
import com.crackmydroid.shared.domain.repository.SecurityAdvisoryRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class LookupApkLibraryVulnerabilitiesUseCaseTest {
    @Test
    fun `deduplicates verifiable libraries and preserves skipped reasons`() = runTest {
        val repository = RecordingRepository()
        val useCase = LookupApkLibraryVulnerabilitiesUseCase(repository)

        val report = useCase(
            listOf(
                VulnerabilityLookupLibrary("OkHttp", "pkg:maven/com.squareup.okhttp3/okhttp@4.12.0", "4.12.0"),
                VulnerabilityLookupLibrary("OkHttp duplicate", "pkg:maven/com.squareup.okhttp3/okhttp@4.12.0", "4.12.0"),
                VulnerabilityLookupLibrary("Firebase", null, "33.0.0"),
                VulnerabilityLookupLibrary("Kotlin", "pkg:maven/org.jetbrains.kotlin/kotlin-stdlib", null),
            ),
        ).getOrThrow()

        assertEquals(1, repository.received.single().size)
        assertEquals(2, report.unverifiedLibraries.size)
        assertEquals(UnverifiedLibraryReason.MissingPackageCoordinate, report.unverifiedLibraries[0].reason)
        assertEquals(UnverifiedLibraryReason.MissingVersion, report.unverifiedLibraries[1].reason)
    }

    @Test
    fun `does not call repository when no library is verifiable`() = runTest {
        val repository = RecordingRepository()
        val useCase = LookupApkLibraryVulnerabilitiesUseCase(repository)

        val report = useCase(listOf(VulnerabilityLookupLibrary("Unknown"))).getOrThrow()

        assertTrue(repository.received.isEmpty())
        assertEquals(AdvisoryLookupAvailability.Available, report.availability)
        assertEquals(UnverifiedLibraryReason.MissingPackageCoordinate, report.unverifiedLibraries.single().reason)
    }

    private class RecordingRepository : SecurityAdvisoryRepository {
        val received = mutableListOf<List<VerifiedVulnerabilityLibrary>>()

        override suspend fun lookup(libraries: List<VerifiedVulnerabilityLibrary>): ApkLibraryVulnerabilityReport {
            received += libraries
            return ApkLibraryVulnerabilityReport(AdvisoryLookupAvailability.Available)
        }
    }
}
