package com.crackmydroid.shared.data.activity

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import androidx.core.net.toUri
import com.crackmydroid.database.CacheDatabase
import com.crackmydroid.shared.data.cache.CachingRepositoryDecorator
import com.crackmydroid.shared.data.cache.PaginationHelper
import com.crackmydroid.shared.domain.model.ComponentType
import com.crackmydroid.shared.domain.model.activity.ActivityEntry
import com.crackmydroid.shared.domain.model.activity.classifyAndroidActivityLaunchability
import com.crackmydroid.shared.domain.model.app.AppComponent
import com.crackmydroid.shared.domain.repository.ActivityRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ActivityRepositoryAndroid(private val context: Context, private val db: CacheDatabase) : ActivityRepository {
    private val cachingDecorator = CachingRepositoryDecorator()
    override suspend fun getActivities(): List<ActivityEntry> = withContext(Dispatchers.IO) {
        val cached = db.cacheDatabaseQueries.selectAllActivities().executeAsList().map {
            val launchability = classifyAndroidActivityLaunchability(
                exported = it.exported == 1L,
                permission = it.permission,
            )
            ActivityEntry(
                label = it.label,
                appLabel = it.appLabel,
                packageName = it.packageName,
                activityName = it.activityName,
                launchableViaShell = launchability.launchableViaShell,
                launchabilityReason = launchability.launchabilityReason,
                exported = it.exported == 1L,
                permission = it.permission,
            )
        }
        if (cached.isNotEmpty()) return@withContext cached

        val pm = context.packageManager
        val packages = runCatching {
            pm.getInstalledPackages(PackageManager.GET_ACTIVITIES)
        }.getOrElse {
            pm.getInstalledPackages(0).mapNotNull { pkg ->
                runCatching { pm.getPackageInfo(pkg.packageName, PackageManager.GET_ACTIVITIES) }.getOrNull()
            }
        }
        val fresh = packages.flatMap { pkgInfo ->
            val activities = pkgInfo.activities ?: emptyArray()
            val appLabel = pkgInfo.applicationInfo
                ?.loadLabel(pm)
                ?.toString()
                ?.takeIf { it.isNotBlank() }
                ?: pkgInfo.packageName
            activities.map { activityInfo ->
                val launchability = classifyAndroidActivityLaunchability(
                    exported = activityInfo.exported,
                    permission = activityInfo.permission,
                )
                val label = activityInfo.loadLabel(pm).toString()
                    .ifBlank { activityInfo.name.substringAfterLast('.') }
                val launchModeStr = when (activityInfo.launchMode) {
                    ActivityInfo.LAUNCH_MULTIPLE -> "standard"
                    ActivityInfo.LAUNCH_SINGLE_TOP -> "singleTop"
                    ActivityInfo.LAUNCH_SINGLE_TASK -> "singleTask"
                    ActivityInfo.LAUNCH_SINGLE_INSTANCE -> "singleInstance"
                    ActivityInfo.LAUNCH_SINGLE_INSTANCE_PER_TASK -> "singleInstancePerTask"
                    else -> null
                }
                ActivityEntry(
                    label = label,
                    appLabel = appLabel,
                    packageName = pkgInfo.packageName,
                    activityName = activityInfo.name,
                    launchableViaShell = launchability.launchableViaShell,
                    launchabilityReason = launchability.launchabilityReason,
                    exported = activityInfo.exported,
                    permission = activityInfo.permission,
                    launchMode = launchModeStr,
                    targetActivity = activityInfo.targetActivity,
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
                    permission = it.permission,
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
        val componentFlags = PackageManager.GET_SERVICES or
            PackageManager.GET_RECEIVERS or
            PackageManager.GET_PROVIDERS
        val packages = runCatching {
            pm.getInstalledPackages(componentFlags)
        }.getOrElse {
            pm.getInstalledPackages(0).mapNotNull { pkg ->
                runCatching { pm.getPackageInfo(pkg.packageName, componentFlags) }.getOrNull()
            }
        }

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
                    permission = service.permission,
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
                    permission = receiver.permission,
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
                    permission = provider.readPermission ?: provider.writePermission,
                )
            }

            components
        }.sortedBy { it.label.lowercase() }
    }

    override suspend fun getComponentsCached(): List<AppComponent> = withContext(Dispatchers.IO) {
        cachingDecorator.getCachedOrCompute(
            cacheGetter = { cachingDecorator.getComponentsCache() },
            cacheSetter = { cachingDecorator.cacheComponents(it) },
            timeGetter = { 0L },
            timeSetter = { },
        ) {
            getComponents()
        }
    }

    override suspend fun launch(
        entry: ActivityEntry,
        action: String?,
        dataUri: String?,
        mimeType: String?,
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
                        setDataAndType(dataUri.toUri(), mimeType)

                    !dataUri.isNullOrBlank() ->
                        data = dataUri.toUri()

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
