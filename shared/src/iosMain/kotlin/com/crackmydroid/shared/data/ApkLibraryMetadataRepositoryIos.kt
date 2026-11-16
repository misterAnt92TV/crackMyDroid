package com.crackmydroid.shared.data

import com.crackmydroid.shared.domain.model.apkcontent.ApkMavenLibraryDiscovery
import com.crackmydroid.shared.domain.repository.ApkLibraryMetadataRepository

class ApkLibraryMetadataRepositoryIos : ApkLibraryMetadataRepository {
    override suspend fun discoverMavenLibraries(packageName: String): Result<ApkMavenLibraryDiscovery> =
        Result.success(ApkMavenLibraryDiscovery(packageName))
}
