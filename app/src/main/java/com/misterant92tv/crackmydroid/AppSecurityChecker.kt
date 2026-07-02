package com.misterant92tv.crackmydroid

import android.Manifest
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build

/**
 * Analyses the security posture of an installed Android application and
 * produces a [SecurityReport].
 */
class AppSecurityChecker(private val packageManager: PackageManager) {

    companion object {
        /**
         * Well-known dangerous permissions defined in the Android platform.
         * Source: https://developer.android.com/reference/android/Manifest.permission
         */
        val DANGEROUS_PERMISSIONS: Set<String> = setOf(
            Manifest.permission.READ_CALENDAR,
            Manifest.permission.WRITE_CALENDAR,
            Manifest.permission.CAMERA,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS,
            Manifest.permission.GET_ACCOUNTS,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_PHONE_NUMBERS,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.ANSWER_PHONE_CALLS,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.WRITE_CALL_LOG,
            Manifest.permission.ADD_VOICEMAIL,
            Manifest.permission.USE_SIP,
            Manifest.permission.PROCESS_OUTGOING_CALLS,
            Manifest.permission.BODY_SENSORS,
            Manifest.permission.ACTIVITY_RECOGNITION,
            Manifest.permission.SEND_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS,
            Manifest.permission.RECEIVE_WAP_PUSH,
            Manifest.permission.RECEIVE_MMS,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_MEDIA_LOCATION,
            Manifest.permission.MANAGE_EXTERNAL_STORAGE,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.UWB_RANGING
        )
    }

    /**
     * Builds a [SecurityReport] for the given [packageName].
     *
     * @throws PackageManager.NameNotFoundException if the package is not installed.
     */
    fun analyse(packageName: String): SecurityReport {
        val flags = PackageManager.GET_ACTIVITIES or
            PackageManager.GET_SERVICES or
            PackageManager.GET_RECEIVERS or
            PackageManager.GET_PROVIDERS or
            PackageManager.GET_PERMISSIONS

        val packageInfo: PackageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(flags.toLong()))
        } else {
            @Suppress("DEPRECATION")
            packageManager.getPackageInfo(packageName, flags)
        }

        val appInfo = packageInfo.applicationInfo
        val appName = packageManager.getApplicationLabel(appInfo).toString()

        val isDebuggable = (appInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        val allowsBackup = (appInfo.flags and ApplicationInfo.FLAG_ALLOW_BACKUP) != 0
        val isTestOnly = (appInfo.flags and ApplicationInfo.FLAG_TEST_ONLY) != 0
        val isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0

        val minSdkVersion: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            appInfo.minSdkVersion
        } else {
            0
        }

        val versionCode: Long = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.longVersionCode
        } else {
            @Suppress("DEPRECATION")
            packageInfo.versionCode.toLong()
        }

        val dangerousPermissions = packageInfo.requestedPermissions
            ?.filter { perm ->
                try {
                    val permInfo = packageManager.getPermissionInfo(perm, 0)
                    val protection = permInfo.protectionLevel and android.content.pm.PermissionInfo.PROTECTION_MASK_BASE
                    protection == android.content.pm.PermissionInfo.PROTECTION_DANGEROUS
                } catch (_: PackageManager.NameNotFoundException) {
                    // Treat unknown permissions conservatively
                    perm in DANGEROUS_PERMISSIONS
                }
            }
            ?.sorted()
            ?: emptyList()

        val exportedActivities = packageInfo.activities
            ?.filter { it.exported }
            ?.map { it.name }
            ?.sorted()
            ?: emptyList()

        val exportedServices = packageInfo.services
            ?.filter { it.exported }
            ?.map { it.name }
            ?.sorted()
            ?: emptyList()

        val exportedReceivers = packageInfo.receivers
            ?.filter { it.exported }
            ?.map { it.name }
            ?.sorted()
            ?: emptyList()

        val exportedProviders = packageInfo.providers
            ?.filter { it.exported }
            ?.map { it.name }
            ?.sorted()
            ?: emptyList()

        val networkSecurityConfigPresent = appInfo.networkSecurityConfigRes != 0

        return SecurityReport(
            packageName = packageName,
            appName = appName,
            versionName = packageInfo.versionName,
            versionCode = versionCode,
            isDebuggable = isDebuggable,
            allowsBackup = allowsBackup,
            isTestOnly = isTestOnly,
            isSystemApp = isSystemApp,
            minSdkVersion = minSdkVersion,
            dangerousPermissions = dangerousPermissions,
            exportedActivities = exportedActivities,
            exportedServices = exportedServices,
            exportedReceivers = exportedReceivers,
            exportedProviders = exportedProviders,
            networkSecurityConfigPresent = networkSecurityConfigPresent
        )
    }

    /**
     * Returns a list of [SecurityReport]s for all user-installed (non-system) packages,
     * sorted by risk score descending (highest risk first).
     */
    fun analyseAllUserApps(): List<SecurityReport> {
        val packages = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getInstalledPackages(PackageManager.PackageInfoFlags.of(0L))
        } else {
            @Suppress("DEPRECATION")
            packageManager.getInstalledPackages(0)
        }

        return packages
            .filter { (it.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0 }
            .mapNotNull { pkg ->
                try {
                    analyse(pkg.packageName)
                } catch (_: Exception) {
                    null
                }
            }
            .sortedByDescending { it.riskScore }
    }

    /**
     * Returns a list of [SecurityReport]s for *all* packages (including system apps),
     * sorted by risk score descending.
     */
    fun analyseAllApps(): List<SecurityReport> {
        val packages = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getInstalledPackages(PackageManager.PackageInfoFlags.of(0L))
        } else {
            @Suppress("DEPRECATION")
            packageManager.getInstalledPackages(0)
        }

        return packages.mapNotNull { pkg ->
            try {
                analyse(pkg.packageName)
            } catch (_: Exception) {
                null
            }
        }.sortedByDescending { it.riskScore }
    }
}
