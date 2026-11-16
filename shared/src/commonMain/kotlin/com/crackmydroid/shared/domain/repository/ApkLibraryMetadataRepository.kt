package com.crackmydroid.shared.domain.repository

import com.crackmydroid.shared.domain.model.apkcontent.ApkMavenLibraryDiscovery

/** Isolated boundary for local Maven metadata extraction used by library advisory checks. */
interface ApkLibraryMetadataRepository {
    suspend fun discoverMavenLibraries(packageName: String): Result<ApkMavenLibraryDiscovery>
}
