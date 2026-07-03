package com.crackmydroid.shared.data.activity

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.content.pm.PackageManager
import com.crackmydroid.shared.domain.model.ActivityEntry
import com.crackmydroid.database.CacheDatabase
import com.crackmydroid.shared.domain.repository.ActivityRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ActivityRepositoryAndroid(
    private val context: Context,
    private val db: CacheDatabase
) : ActivityRepository {
    override suspend fun getActivities(): List<ActivityEntry> = withContext(Dispatchers.IO) {
        val cached = db.cacheDatabaseQueries.selectAllActivities().executeAsList().map {
            ActivityEntry(
                label = it.label,
                appLabel = it.appLabel,
                packageName = it.packageName,
                activityName = it.activityName
            )
        }
        if (cached.isNotEmpty()) return@withContext cached

        val pm = context.packageManager
        val packages = pm.getInstalledPackages(PackageManager.GET_ACTIVITIES)
        val fresh = packages.flatMap { pkgInfo ->
            val activities = pkgInfo.activities ?: emptyArray()
            val appLabel = pkgInfo.applicationInfo
                ?.loadLabel(pm)
                ?.toString()
                ?.takeIf { it.isNotBlank() }
                ?: pkgInfo.packageName
            activities.map { activityInfo ->
                val label = activityInfo.loadLabel(pm).toString()
                    .ifBlank { activityInfo.name.substringAfterLast('.') }
                ActivityEntry(
                    label = label,
                    appLabel = appLabel,
                    packageName = pkgInfo.packageName,
                    activityName = activityInfo.name
                )
            }
        }.sortedBy { it.label.lowercase() }

        db.cacheDatabaseQueries.transaction {
            db.cacheDatabaseQueries.clearActivities()
            fresh.forEach {
                db.cacheDatabaseQueries.insertActivity(
                    packageName = it.packageName,
                    appLabel = it.appLabel,
                    activityName = it.activityName,
                    label = it.label
                )
            }
        }
        fresh
    }

    override suspend fun launch(
        entry: ActivityEntry,
        action: String?,
        dataUri: String?,
        mimeType: String?
    ): Result<Unit> = withContext(Dispatchers.Main) {
        runCatching {
            val intent = Intent().apply {
                component = ComponentName(entry.packageName, entry.activityName)
                if (!action.isNullOrBlank()) {
                    this.action = action
                } else {
                    this.action = Intent.ACTION_MAIN
                    addCategory(Intent.CATEGORY_LAUNCHER)
                }
                when {
                    !dataUri.isNullOrBlank() && !mimeType.isNullOrBlank() ->
                        setDataAndType(Uri.parse(dataUri), mimeType)
                    !dataUri.isNullOrBlank() ->
                        data = Uri.parse(dataUri)
                    !mimeType.isNullOrBlank() ->
                        type = mimeType
                }
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val pm = context.packageManager
            val resolved = pm.resolveActivity(intent, 0)
            checkNotNull(resolved) { "Activity non risolvibile o non esportata" }
            context.startActivity(intent)
        }
    }
}
