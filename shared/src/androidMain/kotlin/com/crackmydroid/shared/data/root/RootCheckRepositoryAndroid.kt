package com.crackmydroid.shared.data.root

import android.content.Context
import android.os.Build
import android.provider.Settings
import com.crackmydroid.shared.domain.model.RootStatus
import com.crackmydroid.shared.domain.repository.RootCheckRepository
import com.crackmydroid.shared.i18n.platformLanguage
import com.crackmydroid.shared.i18n.stringsFor
import com.scottyab.rootbeer.RootBeer
import java.io.File
import java.util.Calendar
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RootCheckRepositoryAndroid(private val context: Context) : RootCheckRepository {
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

    override suspend fun checkRoot(): RootStatus = withContext(Dispatchers.Default) {
        val rootBeer = RootBeer(context)
        val rootSignals = mutableListOf<RootSignal>()
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
        rootSignals += RootSignal(
            label = "Su binary paths",
            detected = suBinaryFound,
            detail = if (suBinaryFound) {
                "Trovato in path noti (${suPaths.count { File(it).exists() }} match)."
            } else {
                "Nessun binario su nei path noti."
            }
        )

        val whichSu = runShellCommand("which su")
        val whichSuDetected = whichSu.success && whichSu.stdout.isNotBlank()
        rootSignals += RootSignal(
            label = "which su",
            detected = whichSuDetected,
            detail = if (whichSuDetected) {
                "Comando disponibile: ${whichSu.stdout.lineSequence().firstOrNull() ?: "su"}"
            } else {
                "Comando su non trovato nella shell."
            }
        )

        val suExec = runShellCommand("su -c id", timeoutMs = 700L)
        val suExecDetected = suExec.success && (suExec.stdout + suExec.stderr).contains("uid=0")
        rootSignals += RootSignal(
            label = "su -c id",
            detected = suExecDetected,
            detail = if (suExecDetected) {
                "Esecuzione privilegiata confermata (${suExec.stdout.ifBlank { "uid=0" }})."
            } else {
                "Nessuna esecuzione root confermata (${suExec.stderr.ifBlank { "accesso negato/timeout" }})."
            }
        )

        val magiskPaths = listOf(
            "/sbin/.magisk",
            "/data/adb/magisk",
            "/cache/.disable_magisk",
            "/data/adb/ksu",
            "/data/adb/modules"
        )
        val magiskFound = magiskPaths.any { path -> File(path).exists() }
        rootSignals += RootSignal(
            label = "Magisk/KernelSU traces",
            detected = magiskFound,
            detail = if (magiskFound) {
                "Tracce trovate in filesystem (${magiskPaths.count { File(it).exists() }} match)."
            } else {
                "Nessuna traccia Magisk/KernelSU rilevata."
            }
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
        rootSignals += RootSignal(
            label = "Root manager apps",
            detected = detectedManagers.isNotEmpty(),
            detail = if (detectedManagers.isNotEmpty()) {
                "App rilevate: ${detectedManagers.joinToString(", ")}"
            } else {
                "Nessun root manager noto rilevato."
            }
        )

        val busyBoxDetected = runCatching { rootBeer.checkForBusyBoxBinary() }.getOrDefault(false)
        rootSignals += RootSignal(
            label = "BusyBox",
            detected = busyBoxDetected,
            detail = if (busyBoxDetected) "BusyBox presente." else "BusyBox non rilevato."
        )

        val testKeys = Build.TAGS?.contains("test-keys") == true
        rootSignals += RootSignal(
            label = "Build test-keys",
            detected = testKeys,
            detail = if (testKeys) "Build firmata con test-keys." else "Build release-keys o tag non sospetti."
        )

        val dangerousProps = runCatching { rootBeer.checkForDangerousProps() }.getOrDefault(false)
        rootSignals += RootSignal(
            label = "Dangerous props",
            detected = dangerousProps,
            detail = if (dangerousProps) "Proprieta sistema sospette rilevate." else "Nessuna proprieta sospetta rilevata."
        )

        val rwPaths = runCatching { rootBeer.checkForRWPaths() }.getOrDefault(false)
        rootSignals += RootSignal(
            label = "RW system paths",
            detected = rwPaths,
            detail = if (rwPaths) "Path di sistema montati RW." else "Path di sistema non RW (o non rilevati)."
        )

        val rootBeerAggregate = runCatching { rootBeer.isRooted }.getOrDefault(false)
        rootSignals += RootSignal(
            label = "RootBeer aggregate",
            detected = rootBeerAggregate,
            detail = if (rootBeerAggregate) "RootBeer segnala rooting." else "RootBeer non segnala rooting."
        )

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

        val adbEnabled = Settings.Global.getInt(context.contentResolver, Settings.Global.ADB_ENABLED, 0) == 1
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

        val rooted = rootSignals.any { signal -> signal.detected }
        val positiveRootSignals = rootSignals.count { signal -> signal.detected }
        val strings = stringsFor(platformLanguage())
        val details = buildString {
            appendLine(if (rooted) strings.rootDetectedDetails else strings.rootNotDetectedDetails)
            appendLine("Indicatori root positivi: $positiveRootSignals/${rootSignals.size}")
            appendLine()
            appendLine("Segnali root:")
            rootSignals.forEach { signal ->
                val marker = if (signal.detected) "[ALERT]" else "[OK]"
                appendLine("$marker ${signal.label}: ${signal.detail}")
            }
            appendLine()
            appendLine("Hardening dispositivo:")
            hardeningSignals.forEach { signal ->
                val marker = if (signal.detected) "[WARN]" else "[OK]"
                appendLine("$marker ${signal.label}: ${signal.detail}")
            }
        }
        RootStatus(rooted, details.trim())
    }

    override suspend fun checkPlayIntegrity(nonce: String): com.crackmydroid.shared.domain.model.PlayIntegrityResult =
        withContext(Dispatchers.IO) {
            try {
                val manager = com.google.android.play.core.integrity.IntegrityManagerFactory.create(context)
                val request = com.google.android.play.core.integrity.IntegrityTokenRequest.builder()
                    .setNonce(nonce)
                    .build()
                // Suspends awaiting the task result
                val token = suspendCoroutine<String> { cont ->
                    manager.requestIntegrityToken(request)
                        .addOnSuccessListener { res -> cont.resume(res.token()) }
                        .addOnFailureListener { err -> cont.resumeWithException(err) }
                }
                com.crackmydroid.shared.domain.model.PlayIntegrityResult(
                    basicIntegrity = null,
                    deviceIntegrity = null,
                    details = stringsFor(platformLanguage()).playIntegrityDetails + ": ${token.take(120)}..."
                )
            } catch (t: Throwable) {
                com.crackmydroid.shared.domain.model.PlayIntegrityResult(
                    basicIntegrity = null,
                    deviceIntegrity = null,
                    details = "Play Integrity error: ${t.message}"
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
