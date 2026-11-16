package com.crackmydroid.shared.domain.usecase.apkcontent

import co.touchlab.kermit.Logger
import com.crackmydroid.architecture.domain.usecase.BaseUseCase
import com.crackmydroid.architecture.logging.AppLogger
import com.crackmydroid.shared.domain.model.apkcontent.AdvisoryLookupAvailability
import com.crackmydroid.shared.domain.model.apkcontent.ApkLibraryVulnerabilityReport
import com.crackmydroid.shared.domain.model.apkcontent.UnverifiedApkLibrary
import com.crackmydroid.shared.domain.model.apkcontent.UnverifiedLibraryReason
import com.crackmydroid.shared.domain.model.apkcontent.VulnerabilityLookupLibrary
import com.crackmydroid.shared.domain.model.apkcontent.VerifiedVulnerabilityLibrary
import com.crackmydroid.shared.domain.repository.SecurityAdvisoryRepository

class LookupApkLibraryVulnerabilitiesUseCase(
    private val repository: SecurityAdvisoryRepository,
    logger: Logger = AppLogger.logger,
) : BaseUseCase(TAG, logger = logger) {
    suspend operator fun invoke(libraries: List<VulnerabilityLookupLibrary>): Result<ApkLibraryVulnerabilityReport> =
        execute {
            val normalized = libraries.map { it to normalize(it) }
            val unverified = normalized.mapNotNull { (library, coordinate) ->
                coordinate?.let { null } ?: unverifiedLibrary(library)
            }.distinct()
            val verifiable = normalized.mapNotNull { (_, coordinate) -> coordinate }
                .groupBy { it.packageUrl to it.version }
                .map { (_, values) -> values.first().copy(aliases = values.flatMap { it.aliases }.distinct()) }
            if (verifiable.isEmpty()) {
                return@execute ApkLibraryVulnerabilityReport(
                    availability = AdvisoryLookupAvailability.Available,
                    unverifiedLibraries = unverified,
                )
            }
            repository.lookup(verifiable).copy(unverifiedLibraries = unverified)
        }

    private fun unverifiedLibrary(library: VulnerabilityLookupLibrary): UnverifiedApkLibrary? = when {
        library.packageUrl.isNullOrBlank() -> UnverifiedApkLibrary(
            displayName = library.displayName,
            reason = UnverifiedLibraryReason.MissingPackageCoordinate,
        )

        library.version.isNullOrBlank() -> UnverifiedApkLibrary(
            displayName = library.displayName,
            reason = UnverifiedLibraryReason.MissingVersion,
        )

        !isValidMavenPurl(library.packageUrl.trim()) -> UnverifiedApkLibrary(
            displayName = library.displayName,
            reason = UnverifiedLibraryReason.InvalidPackageCoordinate,
        )
        else -> null
    }

    private fun normalize(library: VulnerabilityLookupLibrary): VerifiedVulnerabilityLibrary? {
        val purl = library.packageUrl?.trim()?.takeIf(::isValidMavenPurl) ?: return null
        val version = library.version?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        return VerifiedVulnerabilityLibrary(purl, version, aliases = listOf(library.displayName))
    }

    private fun isValidMavenPurl(purl: String): Boolean =
        purl.startsWith("pkg:maven/") && purl.substringAfter("pkg:maven/").contains('/') && purl.contains('@')


    private companion object {
        const val TAG = "LookupApkLibraryVulnerabilitiesUseCase"
    }
}
