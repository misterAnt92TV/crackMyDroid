package com.crackmydroid.shared.data.apps

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.core.content.FileProvider
import com.crackmydroid.shared.domain.model.InstalledAppEntry
import com.crackmydroid.shared.domain.model.ManifestSecurityReport
import com.crackmydroid.shared.domain.model.ManifestSecurityAnalyzer
import com.crackmydroid.shared.domain.model.AppManifestSecurity
import com.crackmydroid.shared.domain.model.ExposedComponentInfo
import com.crackmydroid.shared.domain.model.AppComponentType
import com.crackmydroid.database.CacheDatabase
import com.crackmydroid.shared.domain.repository.InstalledAppsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class InstalledAppsRepositoryAndroid(
    private val context: Context,
    private val db: CacheDatabase
) : InstalledAppsRepository {
    private val cacheDir = File(context.cacheDir, "extracted_apks")

    override suspend fun listInstalled(): List<InstalledAppEntry> = withContext(Dispatchers.IO) {
        val cached = db.cacheDatabaseQueries.selectAllInstalled().executeAsList().map {
            InstalledAppEntry(
                appLabel = it.appLabel,
                packageName = it.packageName,
                sourcePath = it.sourcePath,
                debuggable = it.debuggable ?: false,
                allowBackup = it.allowBackup ?: false,
                testOnly = it.testOnly ?: false,
                targetSdkVersion = it.targetSdkVersion?.toInt(),
                minSdkVersion = it.minSdkVersion?.toInt()
            )
        }
        if (cached.isNotEmpty()) return@withContext cached

        val pm = context.packageManager
        val packages = pm.getInstalledPackages(PackageManager.GET_PERMISSIONS)
        val fresh = packages.map { pkg ->
            val label = pkg.applicationInfo?.loadLabel(pm)?.toString() ?: pkg.packageName
            val source = pkg.applicationInfo?.sourceDir ?: ""
            val debuggable = pkg.applicationInfo?.flags?.and(ApplicationInfo.FLAG_DEBUGGABLE) != 0
            val allowBackup = pkg.applicationInfo?.flags?.and(ApplicationInfo.FLAG_ALLOW_BACKUP) != 0
            val testOnly = pkg.applicationInfo?.flags?.and(ApplicationInfo.FLAG_TEST_ONLY) != 0
            InstalledAppEntry(
                appLabel = label,
                packageName = pkg.packageName,
                sourcePath = source,
                debuggable = debuggable,
                allowBackup = allowBackup,
                testOnly = testOnly,
                targetSdkVersion = pkg.applicationInfo?.targetSdkVersion,
                minSdkVersion = pkg.applicationInfo?.minSdkVersion
            )
        }.sortedBy { it.appLabel.lowercase() }

        db.cacheDatabaseQueries.transaction {
            db.cacheDatabaseQueries.clearInstalled()
            fresh.forEach { entry ->
                db.cacheDatabaseQueries.insertInstalled(
                    packageName = entry.packageName,
                    appLabel = entry.appLabel,
                    sourcePath = entry.sourcePath,
                    debuggable = entry.debuggable,
                    allowBackup = entry.allowBackup,
                    testOnly = entry.testOnly,
                    targetSdkVersion = entry.targetSdkVersion?.toLong(),
                    minSdkVersion = entry.minSdkVersion?.toLong()
                )
            }
        }

        fresh
    }

    override suspend fun analyzeManifestSecurity(): List<ManifestSecurityReport> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val packages = pm.getInstalledPackages(
            PackageManager.GET_PERMISSIONS or 
            PackageManager.GET_SERVICES or 
            PackageManager.GET_RECEIVERS or 
            PackageManager.GET_PROVIDERS
        )
        
        packages.map { pkg ->
            val label = pkg.applicationInfo?.loadLabel(pm)?.toString() ?: pkg.packageName
            val debuggable = pkg.applicationInfo?.flags?.and(ApplicationInfo.FLAG_DEBUGGABLE) != 0
            val allowBackup = pkg.applicationInfo?.flags?.and(ApplicationInfo.FLAG_ALLOW_BACKUP) != 0
            val testOnly = pkg.applicationInfo?.flags?.and(ApplicationInfo.FLAG_TEST_ONLY) != 0
            
            val components = mutableListOf<ExposedComponentInfo>()
            pkg.services?.forEach { service ->
                if (service.exported) {
                    components += ExposedComponentInfo(
                        type = AppComponentType.SERVICE,
                        name = service.name,
                        exported = true,
                        permission = service.permission
                    )
                }
            }
            pkg.receivers?.forEach { receiver ->
                if (receiver.exported) {
                    components += ExposedComponentInfo(
                        type = AppComponentType.RECEIVER,
                        name = receiver.name,
                        exported = true,
                        permission = receiver.permission
                    )
                }
            }
            pkg.providers?.forEach { provider ->
                if (provider.exported) {
                    components += ExposedComponentInfo(
                        type = AppComponentType.PROVIDER,
                        name = provider.name,
                        exported = true,
                        permission = provider.permission
                    )
                }
            }

            val info = AppManifestSecurity(
                appLabel = label,
                packageName = pkg.packageName,
                debuggable = debuggable,
                allowBackup = allowBackup,
                testOnly = testOnly,
                targetSdkVersion = pkg.applicationInfo?.targetSdkVersion,
                minSdkVersion = pkg.applicationInfo?.minSdkVersion,
                exposedComponents = components
            )
            ManifestSecurityAnalyzer.analyze(info)
        }
    }

    override suspend fun exportApk(packageName: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val pm = context.packageManager
            val info = pm.getApplicationInfo(packageName, 0)
            val sourcePath = info.sourceDir ?: error("Percorso APK non disponibile")
            val srcFile = File(sourcePath)
            if (!srcFile.exists()) error("APK non trovato")

            cacheDir.mkdirs()
            val outFile = File(cacheDir, "$packageName.apk")
            srcFile.copyTo(outFile, overwrite = true)
            outFile.absolutePath
        }
    }

    override suspend fun shareApk(packageName: String, bluetoothOnly: Boolean): Result<Unit> =
        withContext(Dispatchers.Main) {
            runCatching {
                val exportedPath = exportApk(packageName).getOrThrow()
                val apkFile = File(exportedPath)
                val uri = FileProvider.getUriForFile(
                    context,
                    context.packageName + ".fileprovider",
                    apkFile
                )

                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/vnd.android.package-archive"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                if (bluetoothOnly) {
                    sendIntent.`package` = "com.android.bluetooth"
                    val resolved = sendIntent.resolveActivity(context.packageManager)
                    if (resolved == null) error("App Bluetooth non trovata")
                    context.startActivity(sendIntent)
                } else {
                    val chooser = Intent.createChooser(sendIntent, null).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(chooser)
                }
            }
        }
}

