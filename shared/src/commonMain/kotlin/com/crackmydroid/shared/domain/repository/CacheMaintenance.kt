package com.crackmydroid.shared.domain.repository

interface CacheMaintenance {
    suspend fun clearAllCaches()
    suspend fun invalidateActivitiesCache()
    suspend fun invalidatePermissionsCache()
    suspend fun invalidateInstalledAppsCache()
    suspend fun invalidateManifestSecurityCache()
}