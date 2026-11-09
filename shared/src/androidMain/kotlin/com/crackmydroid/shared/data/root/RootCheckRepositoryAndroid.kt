package com.crackmydroid.shared.data.root

import android.content.Context
import android.os.Build
import android.provider.Settings
import com.crackmydroid.shared.domain.model.RootStatus
import com.crackmydroid.shared.domain.model.root.RootCheck
import com.crackmydroid.shared.domain.model.root.RootCheckId
import com.crackmydroid.shared.domain.model.root.RootCheckMode
import com.crackmydroid.shared.domain.model.root.RootCheckOutcome
import com.crackmydroid.shared.domain.repository.RootCheckRepository
import com.crackmydroid.shared.domain.repository.IntegrityVerificationRepository
import com.crackmydroid.shared.i18n.platformLanguage
import com.crackmydroid.shared.i18n.stringsFor
import com.scottyab.rootbeer.RootBeer
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Calendar
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class RootCheckRepositoryAndroid(
    private val context: Context,
    private val integrityVerificationRepository: IntegrityVerificationRepository
) : RootCheckRepository {
    private data class RootSignal(
        val label: String,
        val detected: Boolean,
        val detail: String
    )

    private data class ShellCommandOutput(
        val success: Boolean,
        val stdout: String,
        val stderr: String,
        val exitCode: Int?
    )

    override suspend fun checkRoot(mode: RootCheckMode): RootStatus = withContext(Dispatchers.Default) {
        val rootBeer = RootBeer(context)
        val checks = mutableListOf<RootCheck>()
        val hardeningSignals = mutableListOf<RootSignal>()

        val suPaths = listOf(
            "/system/bin/su",
            "/system/xbin/su",
            "/sbin/su",
            "/su/bin/su",
            "/system/app/Superuser.apk",
            "/system/priv-app/Superuser.apk"
        )
        val suBinaryFound = suPaths.any { path -> File(path).exists() }
        val whichSu = runShellCommand("which su")
        val whichSuDetected = whichSu.success && whichSu.stdout.isNotBlank()
        checks += RootCheck(
            RootCheckId.SU_BINARY,
            if (suBinaryFound || whichSuDetected) RootCheckOutcome.DETECTED else RootCheckOutcome.NOT_DETECTED,
            listOf(
                if (suBinaryFound) "${suPaths.count { File(it).exists() }} path match" else "nessun path noto",
                whichSu.stdout.lineSequence().firstOrNull().orEmpty()
            ).filter { it.isNotBlank() }.joinToString("; ")
        )

        val magiskPaths = listOf(
            "/sbin/.magisk",
            "/data/adb/magisk",
            "/cache/.disable_magisk",
            "/data/adb/ksu",
            "/data/adb/modules"
        )
        val magiskFound = magiskPaths.any { path -> File(path).exists() }
        checks += RootCheck(
            RootCheckId.MAGISK_TRACES,
            if (magiskFound) RootCheckOutcome.DETECTED else RootCheckOutcome.NOT_DETECTED,
            "${magiskPaths.count { File(it).exists() }} path match"
        )

        val rootManagers = listOf(
            "com.topjohnwu.magisk",
            "com.topjohnwu.magisk.alpha",
            "eu.chainfire.supersu",
            "com.koushikdutta.superuser",
            "com.noshufou.android.su",
            "com.kingroot.kinguser",
            "com.kingo.root",
            "me.weishu.kernelsu"
        )
        val detectedManagers = rootManagers.filter { pkg -> isPackageInstalled(pkg) }
        checks += RootCheck(
            RootCheckId.ROOT_MANAGER,
            if (detectedManagers.isNotEmpty()) RootCheckOutcome.DETECTED else RootCheckOutcome.NOT_DETECTED,
            detectedManagers.joinToString(", ")
        )

        val busyBoxDetected = runCatching { rootBeer.checkForBusyBoxBinary() }.getOrDefault(false) ||
            runShellCommand("command -v busybox").stdout.isNotBlank()
        val busyBoxList = runShellCommand("busybox --list")
        checks += RootCheck(
            RootCheckId.BUSYBOX,
            if (busyBoxDetected) RootCheckOutcome.DETECTED else RootCheckOutcome.NOT_DETECTED,
            if (busyBoxDetected) "${busyBoxList.stdout.lineSequence().count { it.isNotBlank() }} applet lines" else ""
        )

        val sudo = runShellCommand("command -v sudo")
        val sudoProbe = if (sudo.success && sudo.stdout.isNotBlank()) {
            runShellCommand("sudo -n id", timeoutMs = 700L)
        } else null
        checks += RootCheck(
            RootCheckId.SUDO,
            when {
                sudoProbe?.stdout?.contains("uid=0") == true -> RootCheckOutcome.DETECTED
                sudo.success && sudo.stdout.isNotBlank() -> RootCheckOutcome.DENIED
                else -> RootCheckOutcome.NOT_DETECTED
            },
            listOf(sudo.stdout, sudoProbe?.stderr.orEmpty()).filter { it.isNotBlank() }.joinToString(" ")
        )

        if (mode == RootCheckMode.PRIVILEGE_ACCESS) {
            val suExec = runShellCommand("su -c id", timeoutMs = 5_000L)
            val output = suExec.stdout + suExec.stderr
            val outcome = when {
                output.contains("uid=0") -> RootCheckOutcome.DETECTED
                suExec.stderr.contains("timeout", ignoreCase = true) -> RootCheckOutcome.TIMEOUT
                else -> RootCheckOutcome.DENIED
            }
            checks += RootCheck(RootCheckId.ROOT_ACCESS, outcome, output.take(240))
        } else {
            checks += RootCheck(
                RootCheckId.ROOT_ACCESS,
                RootCheckOutcome.UNAVAILABLE,
                "prova esplicita non eseguita"
            )
        }

        val testKeys = Build.TAGS?.contains("test-keys") == true

        val dangerousProps = runCatching { rootBeer.checkForDangerousProps() }.getOrDefault(false)
        hardeningSignals += RootSignal(
            label = "Dangerous props",
            detected = dangerousProps,
            detail = if (dangerousProps) "Proprieta sistema sospette rilevate." else "Nessuna proprieta sospetta rilevata."
        )

        val rwPaths = runCatching { rootBeer.checkForRWPaths() }.getOrDefault(false)
        hardeningSignals += RootSignal(
            label = "RW system paths",
            detected = rwPaths,
            detail = if (rwPaths) "Path di sistema montati RW." else "Path di sistema non RW (o non rilevati)."
        )

        val rootBeerAggregate = runCatching { rootBeer.isRooted }.getOrDefault(false)
        checks += RootCheck(
            RootCheckId.ROOTBEER,
            if (rootBeerAggregate) RootCheckOutcome.DETECTED else RootCheckOutcome.NOT_DETECTED,
            "RootBeer aggregate"
        )
        hardeningSignals += RootSignal("Build test-keys", testKeys, "test-keys=$testKeys")

        val selinuxFlag = readFirstLine("/sys/fs/selinux/enforce")
        val selinuxPermissive = selinuxFlag == "0"
        hardeningSignals += RootSignal(
            label = "SELinux",
            detected = selinuxPermissive,
            detail = when (selinuxFlag) {
                "1" -> "Enforcing"
                "0" -> "Permissive"
                else -> "Stato non disponibile"
            }
        )

        val adbEnabled =
            Settings.Global.getInt(context.contentResolver, Settings.Global.ADB_ENABLED, 0) == 1
        hardeningSignals += RootSignal(
            label = "ADB debugging",
            detected = adbEnabled,
            detail = if (adbEnabled) "Attivo" else "Disattivo"
        )

        val developerOptionsEnabled = Settings.Global.getInt(
            context.contentResolver,
            Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
            0
        ) == 1
        hardeningSignals += RootSignal(
            label = "Developer options",
            detected = developerOptionsEnabled,
            detail = if (developerOptionsEnabled) "Attive" else "Disattivate"
        )

        val securityPatch = Build.VERSION.SECURITY_PATCH ?: "unknown"
        val patchYear = securityPatch.take(4).toIntOrNull()
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val patchOutdated = patchYear == null || patchYear <= currentYear - 2
        hardeningSignals += RootSignal(
            label = "Security patch",
            detected = patchOutdated,
            detail = if (patchOutdated) {
                "Patch vecchia/non disponibile ($securityPatch)."
            } else {
                "Patch dichiarata: $securityPatch"
            }
        )

        val rooted = checks.any { it.id == RootCheckId.ROOT_ACCESS && it.outcome == RootCheckOutcome.DETECTED }
        val positiveRootSignals = checks.count { it.outcome == RootCheckOutcome.DETECTED }
        val strings = stringsFor(platformLanguage())
        val details = buildString {
            appendLine(if (rooted) strings.rootDetectedDetails else strings.rootNotDetectedDetails)
            appendLine("Indicatori root rilevati: $positiveRootSignals/${checks.size}")
            appendLine()
            checks.forEach { check -> appendLine("${check.id}: ${check.outcome} ${check.evidence}") }
            appendLine()
            appendLine("Hardening dispositivo:")
            hardeningSignals.forEach { signal ->
                val marker = if (signal.detected) "[WARN]" else "[OK]"
                appendLine("$marker ${signal.label}: ${signal.detail}")
            }
        }
        RootStatus(rooted, details.trim(), checks)
    }

    override suspend fun checkPlayIntegrity(nonce: String): com.crackmydroid.shared.domain.model.PlayIntegrityResult =
        withContext(Dispatchers.IO) {
            try {
                val manager =
                    com.google.android.play.core.integrity.IntegrityManagerFactory.create(context)
                val request = com.google.android.play.core.integrity.IntegrityTokenRequest.builder()
                    .setNonce(nonce)
                    .build()
                val token = suspendCancellableCoroutine<String> { cont ->
                    manager.requestIntegrityToken(request)
                        .addOnSuccessListener { res ->
                            if (cont.isActive) cont.resume(res.token())
                        }
                        .addOnFailureListener { err ->
                            if (cont.isActive) cont.resumeWithException(err)
                        }
                }
                integrityVerificationRepository.verify(token, nonce)
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                com.crackmydroid.shared.domain.model.PlayIntegrityResult(
                    basicIntegrity = null,
                    deviceIntegrity = null,
                    details = "Play Integrity error: ${error.message}"
                )
            }
        }

    private fun isPackageInstalled(packageName: String): Boolean {
        @Suppress("DEPRECATION")
        return runCatching {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        }.getOrDefault(false)
    }

    private fun readFirstLine(path: String): String? =
        runCatching {
            val file = File(path)
            if (!file.exists() || !file.canRead()) return@runCatching null
            file.bufferedReader().use { reader -> reader.readLine()?.trim() }
        }.getOrNull()

    private fun runShellCommand(command: String, timeoutMs: Long = 450L): ShellCommandOutput {
        return runCatching {
            val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
            val start = System.currentTimeMillis()
            var exitCode: Int? = null
            while (exitCode == null && (System.currentTimeMillis() - start) < timeoutMs) {
                exitCode = runCatching { process.exitValue() }.getOrNull()
                if (exitCode == null) {
                    Thread.sleep(25)
                }
            }

            if (exitCode == null) {
                process.destroy()
                return@runCatching ShellCommandOutput(
                    success = false,
                    stdout = "",
                    stderr = "timeout",
                    exitCode = null
                )
            }

            val stdout = process.inputStream.bufferedReader().use { it.readText().trim() }
            val stderr = process.errorStream.bufferedReader().use { it.readText().trim() }
            ShellCommandOutput(
                success = exitCode == 0,
                stdout = stdout,
                stderr = stderr,
                exitCode = exitCode
            )
        }.getOrElse { err ->
            ShellCommandOutput(
                success = false,
                stdout = "",
                stderr = err.message ?: "errore shell",
                exitCode = null
            )
        }
    }
}
