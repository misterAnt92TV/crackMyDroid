package com.crackmydroid.shared.data.root

import com.crackmydroid.shared.data.DesktopAdbRepositorySupport
import com.crackmydroid.shared.data.DesktopScanCache
import com.crackmydroid.shared.data.adb.AdbParsers
import com.crackmydroid.shared.domain.model.PlayIntegrityResult
import com.crackmydroid.shared.domain.model.RootStatus
import com.crackmydroid.shared.domain.repository.AdbBridge
import com.crackmydroid.shared.domain.repository.DeviceSessionController
import com.crackmydroid.shared.domain.repository.RootCheckRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

class RootCheckRepositoryDesktop(
    adbBridge: AdbBridge,
    sessionController: DeviceSessionController,
    private val cache: DesktopScanCache
) : DesktopAdbRepositorySupport(adbBridge, sessionController), RootCheckRepository {
    override suspend fun checkRoot(): RootStatus = withContext(Dispatchers.Default) {
        cache.rootStatus?.let { return@withContext it }

        val propsText = shell("getprop")
        val props = AdbParsers.parseGetProp(propsText)
        val suPaths = listOf(
            "/system/bin/su",
            "/system/xbin/su",
            "/sbin/su",
            "/su/bin/su",
            "/system/app/Superuser.apk",
            "/system/priv-app/Superuser.apk"
        )
        val suBinaryFound = suPaths.any { path ->
            shell("[ -e '$path' ] && echo 1 || echo 0") == "1"
        }
        val whichSu = shell("which su")
        val suExec = runCatching { shell("id", asRoot = true) }.getOrDefault("")
        val magiskTraces = listOf(
            "/sbin/.magisk",
            "/data/adb/magisk",
            "/data/adb/ksu",
            "/data/adb/modules"
        )
        val magiskFound = magiskTraces.any { path ->
            shell("[ -e '$path' ] && echo 1 || echo 0") == "1"
        }
        val dangerousProps = props["ro.debuggable"] == "1" || props["ro.secure"] == "0"
        val testKeys = props["ro.build.tags"]?.contains("test-keys") == true
        val rwSystem = runCatching {
            shell("mount | grep ' /system ' | grep rw")
        }.getOrNull()?.isNotBlank() == true
        val selinux = runCatching { shell("getenforce") }.getOrDefault("unknown")
        val adbEnabled = shell("settings get global adb_enabled").trim() == "1"
        val developerOptions = shell("settings get global development_settings_enabled").trim() == "1"
        val securityPatch = props["ro.build.version.security_patch"] ?: "unknown"
        val patchYear = securityPatch.take(4).toIntOrNull()
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val outdatedPatch = patchYear == null || patchYear <= currentYear - 2
        val rootedSignals = listOf(
            "Su binary paths" to suBinaryFound,
            "which su" to whichSu.isNotBlank(),
            "su -c id" to suExec.contains("uid=0"),
            "Magisk/KernelSU traces" to magiskFound,
            "Build test-keys" to testKeys,
            "Dangerous props" to dangerousProps,
            "RW system paths" to rwSystem
        )
        val rooted = rootedSignals.any { it.second }
        val details = buildString {
            appendLine(if (rooted) "Possibili tracce di root rilevate." else "Nessuna traccia di root evidente.")
            appendLine("Indicatori root positivi: ${rootedSignals.count { it.second }}/${rootedSignals.size}")
            appendLine()
            rootedSignals.forEach { (label, detected) ->
                appendLine("${if (detected) "[ALERT]" else "[OK]"} $label")
            }
            appendLine()
            appendLine("${if (selinux.equals("Enforcing", true)) "[OK]" else "[WARN]"} SELinux: $selinux")
            appendLine("${if (adbEnabled) "[WARN]" else "[OK]"} ADB debugging: ${if (adbEnabled) "Attivo" else "Disattivo"}")
            appendLine("${if (developerOptions) "[WARN]" else "[OK]"} Developer options: ${if (developerOptions) "Attive" else "Disattivate"}")
            appendLine("${if (outdatedPatch) "[WARN]" else "[OK]"} Security patch: $securityPatch")
        }.trim()
        RootStatus(rooted, details).also {
            cache.rootStatus = it
        }
    }

    override suspend fun checkPlayIntegrity(nonce: String): PlayIntegrityResult =
        cache.playIntegrity ?: PlayIntegrityResult(
            basicIntegrity = null,
            deviceIntegrity = null,
            details = "Non disponibile in modalita desktop adb-only"
        ).also {
            cache.playIntegrity = it
        }
}
