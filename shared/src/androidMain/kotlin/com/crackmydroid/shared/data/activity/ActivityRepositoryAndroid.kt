package com.crackmydroid.shared.data.activity

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.content.pm.PackageManager
import com.crackmydroid.shared.domain.model.ActivityEntry
import com.crackmydroid.shared.domain.model.AppComponent
import com.crackmydroid.shared.domain.model.ComponentType
import com.crackmydroid.database.CacheDatabase
import com.crackmydroid.shared.domain.repository.ActivityRepository
import com.crackmydroid.shared.data.cache.CachingRepositoryDecorator
import android.content.pm.ActivityInfo
import com.crackmydroid.shared.data.cache.PaginationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ActivityRepositoryAndroid(
    private val context: Context,
    private val db: CacheDatabase
) : ActivityRepository {
    private val cachingDecorator = CachingRepositoryDecorator()
    override suspend fun getActivities(): List<ActivityEntry> = withContext(Dispatchers.IO) {
        val cached = db.cacheDatabaseQueries.selectAllActivities().executeAsList().map {
            ActivityEntry(
                label = it.label,
                appLabel = it.appLabel,
                packageName = it.packageName,
                activityName = it.activityName,
                exported = it.exported == 1L,
                permission = it.permission
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
                    activityName = activityInfo.name,
                    exported = activityInfo.exported,
                    permission = activityInfo.permission
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
                    label = it.label,
                    exported = if (it.exported) 1L else 0L,
                    permission = it.permission
                )
            }
        }
        fresh
    }

    override suspend fun getActivitiesPaginated(offset: Int, limit: Int): List<ActivityEntry> =
        withContext(Dispatchers.IO) {
            val all = getActivities()
            PaginationHelper.paginate(all, offset, limit)
        }

    override suspend fun getComponents(): List<AppComponent> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val packages = pm.getInstalledPackages(
            PackageManager.GET_SERVICES or 
            PackageManager.GET_RECEIVERS or 
            PackageManager.GET_PROVIDERS
        )
        
        packages.flatMap { pkgInfo ->
            val appLabel = pkgInfo.applicationInfo
                ?.loadLabel(pm)
                ?.toString()
                ?.takeIf { it.isNotBlank() }
                ?: pkgInfo.packageName
            val components = mutableListOf<AppComponent>()
            
            pkgInfo.services?.forEach { service ->
                components += AppComponent(
                    label = service.name.substringAfterLast('.'),
                    appLabel = appLabel,
                    packageName = pkgInfo.packageName,
                    name = service.name,
                    type = ComponentType.SERVICE,
                    exported = service.exported,
                    permission = service.permission
                )
            }
            
            pkgInfo.receivers?.forEach { receiver ->
                components += AppComponent(
                    label = receiver.name.substringAfterLast('.'),
                    appLabel = appLabel,
                    packageName = pkgInfo.packageName,
                    name = receiver.name,
                    type = ComponentType.RECEIVER,
                    exported = receiver.exported,
                    permission = receiver.permission
                )
            }
            
            pkgInfo.providers?.forEach { provider ->
                components += AppComponent(
                    label = provider.name.substringAfterLast('.'),
                    appLabel = appLabel,
                    packageName = pkgInfo.packageName,
                    name = provider.name,
                    type = ComponentType.PROVIDER,
                    exported = provider.exported,
                    permission = provider.readPermission ?: provider.writePermission
                )
            }
            
            components
        }.sortedBy { it.label.lowercase() }
    }

    override suspend fun getComponentsCached(): List<AppComponent> =
        withContext(Dispatchers.IO) {
            cachingDecorator.getCachedOrCompute(
                cacheGetter = { cachingDecorator.getComponentsCache() },
                cacheSetter = { cachingDecorator.cacheComponents(it) },
                timeGetter = { 0L },
                timeSetter = { }
            ) {
                getComponents()
            }
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
