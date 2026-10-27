package com.crackmydroid.shared.data.cache

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import com.crackmydroid.database.CacheDatabase
import com.crackmydroid.shared.domain.repository.CacheMaintenance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PackageChangeReceiver(private val cacheMaintenance: CacheMaintenance) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_PACKAGE_ADDED,
            Intent.ACTION_PACKAGE_REPLACED,
            Intent.ACTION_PACKAGE_REMOVED -> {
                goAsync().apply {
                    try {
                        kotlinx.coroutines.runBlocking {
                            cacheMaintenance.clearAllCaches()
                        }
                    } finally {
                        finish()
                    }
                }
            }
        }
    }

    companion object {
        fun registerReceiver(context: Context, cacheMaintenance: CacheMaintenance) {
            val receiver = PackageChangeReceiver(cacheMaintenance)
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_PACKAGE_ADDED)
                addAction(Intent.ACTION_PACKAGE_REPLACED)
                addAction(Intent.ACTION_PACKAGE_REMOVED)
                addDataScheme("package")
            }
            ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
        }
    }
}

class CacheMaintenanceRepositoryAndroid(
    private val db: CacheDatabase
) : CacheMaintenance {
    override suspend fun clearAllCaches() = withContext(Dispatchers.IO) {
        db.cacheDatabaseQueries.transaction {
            db.cacheDatabaseQueries.clearActivities()
            db.cacheDatabaseQueries.clearPermissions()
            db.cacheDatabaseQueries.clearInstalled()
            db.cacheDatabaseQueries.clearDeviceInfo()
        }
    }

    override suspend fun invalidateActivitiesCache() = withContext(Dispatchers.IO) {
        db.cacheDatabaseQueries.clearActivities()
    }

    override suspend fun invalidatePermissionsCache() = withContext(Dispatchers.IO) {
        db.cacheDatabaseQueries.clearPermissions()
    }

    override suspend fun invalidateInstalledAppsCache() = withContext(Dispatchers.IO) {
        db.cacheDatabaseQueries.clearInstalled()
    }

    override suspend fun invalidateManifestSecurityCache() = withContext(Dispatchers.IO) {
        // No dedicated table, but clearing installed apps implicitly invalidates manifest analysis
    }
}
