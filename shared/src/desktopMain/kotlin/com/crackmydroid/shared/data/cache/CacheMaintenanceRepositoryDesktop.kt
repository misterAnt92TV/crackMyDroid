package com.crackmydroid.shared.data.cache

import com.crackmydroid.shared.domain.repository.CacheMaintenance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CacheMaintenanceRepositoryDesktop : CacheMaintenance {
    override suspend fun clearAllCaches() = withContext(Dispatchers.IO) {
        invalidateActivitiesCache()
        invalidatePermissionsCache()
        invalidateInstalledAppsCache()
        invalidateManifestSecurityCache()
    }

    override suspend fun invalidateActivitiesCache() = withContext(Dispatchers.IO) {
        // Desktop uses DesktopScanCache, clearing happens at session level
    }

    override suspend fun invalidatePermissionsCache() = withContext(Dispatchers.IO) {
        // Desktop uses DesktopScanCache, clearing happens at session level
    }

    override suspend fun invalidateInstalledAppsCache() = withContext(Dispatchers.IO) {
        // Desktop uses DesktopScanCache, clearing happens at session level
    }

    override suspend fun invalidateManifestSecurityCache() = withContext(Dispatchers.IO) {
        // Desktop uses DesktopScanCache, clearing happens at session level
    }
}
