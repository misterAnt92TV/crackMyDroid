package com.crackmydroid.shared.data.activity

import com.crackmydroid.shared.data.DesktopAdbRepositorySupport
import com.crackmydroid.shared.data.DesktopScanCache
import com.crackmydroid.shared.data.adb.AdbParsers
import com.crackmydroid.shared.domain.model.ActivityEntry
import com.crackmydroid.shared.domain.repository.ActivityRepository
import com.crackmydroid.shared.domain.repository.AdbBridge
import com.crackmydroid.shared.domain.repository.DeviceSessionController
import com.crackmydroid.shared.domain.repository.RemotePackageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ActivityRepositoryDesktop(
    adbBridge: AdbBridge,
    sessionController: DeviceSessionController,
    private val cache: DesktopScanCache,
    private val packageManager: RemotePackageManager
) : DesktopAdbRepositorySupport(adbBridge, sessionController), ActivityRepository {
    override suspend fun getActivities(): List<ActivityEntry> = withContext(Dispatchers.IO) {
        cache.activities?.let { return@withContext it }

        val installedApps = packageManager.listInstalledApps()
        val fresh = installedApps.flatMap { app ->
            val dump = packageManager.readPackageDump(app.packageName)
            AdbParsers.parseActivities(app.packageName, app.appLabel, dump)
        }.sortedBy { it.label.lowercase() }
        cache.activities = fresh
        fresh
    }

    override suspend fun launch(
        entry: ActivityEntry,
        action: String?,
        dataUri: String?,
        mimeType: String?
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val normalizedAction = action?.trim().orEmpty().ifBlank { null }
            val normalizedData = dataUri?.trim().orEmpty().ifBlank { null }
            val normalizedMimeType = mimeType?.trim().orEmpty().ifBlank { null }
            val hasGuidedIntent = normalizedAction != null || normalizedData != null || normalizedMimeType != null
            require(entry.launchableViaShell || hasGuidedIntent) {
                entry.launchabilityReason ?: "Activity visibile nel dump ma non avviabile via am start -n"
            }
            val component = shellQuote("${entry.packageName}/${entry.activityName}")
            val command = buildString {
                append("am start -n ")
                append(component)
                normalizedAction?.let {
                    append(" -a ")
                    append(shellQuote(it))
                }
                normalizedData?.let {
                    append(" -d ")
                    append(shellQuote(it))
                }
                normalizedMimeType?.let {
                    append(" -t ")
                    append(shellQuote(it))
                }
            }
            shell(command)
        }.map { Unit }
    }

    private fun shellQuote(value: String): String =
        "'${value.replace("'", "'\\''")}'"
}
