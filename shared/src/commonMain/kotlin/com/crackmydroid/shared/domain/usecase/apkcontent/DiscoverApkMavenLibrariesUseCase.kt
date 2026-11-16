package com.crackmydroid.shared.domain.usecase.apkcontent

import co.touchlab.kermit.Logger
import com.crackmydroid.architecture.domain.usecase.BaseUseCase
import com.crackmydroid.architecture.logging.AppLogger
import com.crackmydroid.shared.domain.model.apkcontent.ApkMavenLibraryDiscovery
import com.crackmydroid.shared.domain.repository.ApkLibraryMetadataRepository

class DiscoverApkMavenLibrariesUseCase(
    private val repository: ApkLibraryMetadataRepository,
    logger: Logger = AppLogger.logger,
) : BaseUseCase(TAG, logger = logger) {
    suspend operator fun invoke(packageName: String): Result<ApkMavenLibraryDiscovery> = execute {
        require(packageName.isNotBlank()) { "Package name is required" }
        repository.discoverMavenLibraries(packageName).getOrThrow()
    }

    private companion object { const val TAG = "DiscoverApkMavenLibrariesUseCase" }
}
