package com.crackmydroid.shared.data.root

import com.crackmydroid.shared.data.DesktopAdbRepositorySupport
import com.crackmydroid.shared.data.DesktopScanCache
import com.crackmydroid.shared.data.adb.AdbParsers
import com.crackmydroid.shared.domain.model.PlayIntegrityResult
import com.crackmydroid.shared.domain.model.RootStatus
import com.crackmydroid.shared.domain.model.root.RootCheck
import com.crackmydroid.shared.domain.model.root.RootCheckId
import com.crackmydroid.shared.domain.model.root.RootCheckMode
import com.crackmydroid.shared.domain.model.root.RootCheckOutcome
import com.crackmydroid.shared.domain.repository.AdbBridge
import com.crackmydroid.shared.domain.repository.DeviceSessionController
import com.crackmydroid.shared.domain.repository.RootCheckRepository
import com.crackmydroid.shared.util.runSuspendCatching
import java.util.Calendar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RootCheckRepositoryDesktop(
    adbBridge: AdbBridge,
    sessionController: DeviceSessionController,
    private val cache: DesktopScanCache,
) : DesktopAdbRepositorySupport(adbBridge, sessionController),
    RootCheckRepository {
    override suspend fun checkRoot(mode: RootCheckMode): RootStatus = withContext(Dispatchers.Default) {
        if (mode == RootCheckMode.PASSIVE) cache.rootStatus?.let { return@withContext it }
        val checks = mutableListOf<RootCheck>()

        val propsText = shell("getprop")
        val props = AdbParsers.parseGetProp(propsText)
        val suPaths = listOf(
            "/system/bin/su",
            "/system/xbin/su",
            "/sbin/su",
            "/su/bin/su",
            "/system/app/Superuser.apk",
            "/system/priv-app/Superuser.apk",
        )
        val suBinaryFound = suPaths.any { path ->
            shell("[ -e '$path' ] && echo 1 || echo 0") == "1"
        }
        val whichSu = shell("which su")
        val suExec = if (mode == RootCheckMode.PRIVILEGE_ACCESS) {
            runSuspendCatching { shell("id", asRoot = true, timeoutMs = 5_000L) }.getOrDefault("")
        } else {
            ""
        }
        val magiskTraces = listOf(
            "/sbin/.magisk",
            "/data/adb/magisk",
            "/data/adb/ksu",
            "/data/adb/modules",
        )
        val magiskFound = magiskTraces.any { path ->
            shell("[ -e '$path' ] && echo 1 || echo 0") == "1"
        }
        val dangerousProps = props["ro.debuggable"] == "1" || props["ro.secure"] == "0"
        val testKeys = props["ro.build.tags"]?.contains("test-keys") == true
        val rwSystem = runSuspendCatching {
            shell("mount | grep ' /system ' | grep rw")
        }.getOrNull()?.isNotBlank() == true
        val selinux = runSuspendCatching { shell("getenforce") }.getOrDefault("unknown")
        val adbEnabled = shell("settings get global adb_enabled").trim() == "1"
        val developerOptions =
            shell("settings get global development_settings_enabled").trim() == "1"
        val securityPatch = props["ro.build.version.security_patch"] ?: "unknown"
        val patchYear = securityPatch.take(4).toIntOrNull()
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val outdatedPatch = patchYear == null || patchYear <= currentYear - 2
        checks += RootCheck(
            RootCheckId.SU_BINARY,
            if (suBinaryFound || whichSu.isNotBlank()) RootCheckOutcome.DETECTED else RootCheckOutcome.NOT_DETECTED,
        )
        checks += RootCheck(
            RootCheckId.MAGISK_TRACES,
            if (magiskFound) RootCheckOutcome.DETECTED else RootCheckOutcome.NOT_DETECTED,
        )
        val busyBox = runSuspendCatching { shell("command -v busybox") }.getOrDefault("")
        val busyBoxList = runSuspendCatching { shell("busybox --list") }.getOrDefault("")
        checks += RootCheck(
            RootCheckId.BUSYBOX,
            if (busyBox.isNotBlank()) RootCheckOutcome.DETECTED else RootCheckOutcome.NOT_DETECTED,
            busyBoxList.lines().count { it.isNotBlank() }.toString(),
        )
        val sudo = runSuspendCatching { shell("command -v sudo") }.getOrDefault("")
        val sudoProbe = if (sudo.isNotBlank()) runSuspendCatching { shell("sudo -n id") }.getOrDefault("") else ""
        checks += RootCheck(
            RootCheckId.SUDO,
            when {
                sudoProbe.contains("uid=0") -> RootCheckOutcome.DETECTED
                sudo.isNotBlank() -> RootCheckOutcome.DENIED
                else -> RootCheckOutcome.NOT_DETECTED
            },
            listOf(sudo, sudoProbe).filter { it.isNotBlank() }.joinToString(" "),
        )
        val managerPackages =
            runSuspendCatching { shell("pm list packages | grep -E 'magisk|supersu|superuser|kernelsu'") }.getOrDefault(
                "",
            )
        checks += RootCheck(
            RootCheckId.ROOT_MANAGER,
            if (managerPackages.isNotBlank()) RootCheckOutcome.DETECTED else RootCheckOutcome.NOT_DETECTED,
            managerPackages,
        )
        val suOutcome = if (mode == RootCheckMode.PRIVILEGE_ACCESS) {
            when {
                suExec.contains("uid=0") -> RootCheckOutcome.DETECTED
                suExec.isBlank() -> RootCheckOutcome.DENIED
                else -> RootCheckOutcome.NOT_DETECTED
            }
        } else {
            RootCheckOutcome.UNAVAILABLE
        }
        checks += RootCheck(
            RootCheckId.ROOT_ACCESS,
            suOutcome,
            if (mode == RootCheckMode.PASSIVE) "prova esplicita non eseguita" else suExec.take(240),
        )
        val rooted = suOutcome == RootCheckOutcome.DETECTED
        val details = buildString {
            appendLine(if (rooted) "Possibili tracce di root rilevate." else "Nessuna traccia di root evidente.")
            appendLine(
                "Indicatori rilevati: ${checks.count {
                    it.outcome == RootCheckOutcome.DETECTED
                }}/${checks.size}",
            )
            appendLine()
            checks.forEach { check ->
                appendLine("${check.id}: ${check.outcome} ${check.evidence}")
            }
            appendLine()
            appendLine(
                "${
                    if (selinux.equals(
                            "Enforcing",
                            true,
                        )
                    ) {
                        "[OK]"
                    } else {
                        "[WARN]"
                    }
                } SELinux: $selinux",
            )
            appendLine(
                "${if (adbEnabled) "[WARN]" else "[OK]"} ADB debugging: ${if (adbEnabled) "Attivo" else "Disattivo"}",
            )
            appendLine(
                "${if (developerOptions) "[WARN]" else "[OK]"} Developer options: ${if (developerOptions) "Attive" else "Disattivate"}",
            )
            appendLine("${if (outdatedPatch) "[WARN]" else "[OK]"} Security patch: $securityPatch")
        }.trim()
        RootStatus(rooted, details, checks).also { result ->
            if (mode == RootCheckMode.PASSIVE) cache.rootStatus = result
        }
    }

    override suspend fun checkPlayIntegrity(nonce: String): PlayIntegrityResult =
        cache.playIntegrity ?: PlayIntegrityResult(
            basicIntegrity = null,
            deviceIntegrity = null,
            details = "Non disponibile in modalita desktop adb-only",
        ).also {
            cache.playIntegrity = it
        }
}
