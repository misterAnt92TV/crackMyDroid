package com.crackmydroid.shared.data.permissions

import android.content.Context
import android.content.pm.PackageManager
import com.crackmydroid.shared.domain.model.AppPermissionEntry
import com.crackmydroid.database.CacheDatabase
import com.crackmydroid.shared.domain.repository.PermissionsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PermissionsRepositoryAndroid(
    private val context: Context,
    private val db: CacheDatabase
) : PermissionsRepository {
    override suspend fun listAppPermissions(): List<AppPermissionEntry> = withContext(Dispatchers.IO) {
        val cached = db.cacheDatabaseQueries.selectAllPermissions().executeAsList().groupBy { it.packageName }
        if (cached.isNotEmpty()) {
            return@withContext cached.map { (pkg, rows) ->
                AppPermissionEntry(
                    appLabel = rows.firstOrNull()?.appLabel ?: pkg,
                    packageName = pkg,
                    permissions = rows.map { it.permission }
                )
            }.sortedBy { it.appLabel.lowercase() }
        }

        val pm = context.packageManager
        val packages = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS)
        val fresh = packages.map { pkg ->
            val label = pkg.applicationInfo?.loadLabel(pm)?.toString() ?: pkg.packageName
            val perms = pkg.requestedPermissions?.toList().orEmpty()
            AppPermissionEntry(
                appLabel = label,
                packageName = pkg.packageName,
                permissions = perms
            )
        }.sortedBy { it.appLabel.lowercase() }

        db.cacheDatabaseQueries.transaction {
            db.cacheDatabaseQueries.clearPermissions()
            fresh.forEach { entry ->
                entry.permissions.forEach { perm ->
                    db.cacheDatabaseQueries.insertPermission(
                        packageName = entry.packageName,
                        appLabel = entry.appLabel,
                        permission = perm
                    )
                }
            }
        }

        fresh
    }
}
