package com.crackmydroid.shared.data.shell

import com.crackmydroid.shared.data.DesktopAdbRepositorySupport
import com.crackmydroid.shared.data.DesktopScanCache
import com.crackmydroid.shared.domain.model.ShellCommand
import com.crackmydroid.shared.domain.model.ShellCommandResult
import com.crackmydroid.shared.domain.repository.AdbBridge
import com.crackmydroid.shared.domain.repository.DeviceSessionController
import com.crackmydroid.shared.domain.repository.ShellCommandRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ShellCommandRepositoryDesktop(
    adbBridge: AdbBridge,
    sessionController: DeviceSessionController,
    private val cache: DesktopScanCache
) : DesktopAdbRepositorySupport(adbBridge, sessionController), ShellCommandRepository {
    private val commands = listOf(
        ShellCommand("Riavvia", "reboot", requiresRoot = true, confirmation = "Riavviare il dispositivo?"),
        ShellCommand("Force stop SystemUI", "am force-stop com.android.systemui", requiresRoot = true, confirmation = "Forzare l'arresto di SystemUI?"),
        ShellCommand("Toggle animazioni 0", "settings put global window_animation_scale 0; settings put global transition_animation_scale 0; settings put global animator_duration_scale 0", true),
        ShellCommand("Toggle animazioni 1", "settings put global window_animation_scale 1; settings put global transition_animation_scale 1; settings put global animator_duration_scale 1", true),
        ShellCommand("Attiva debug USB", "settings put global adb_enabled 1", true),
        ShellCommand("Disattiva debug USB", "settings put global adb_enabled 0", true),
        ShellCommand("Mostra top process", "top -n 1", false),
        ShellCommand("Elenco pacchetti", "pm list packages", false),
        ShellCommand("Riavvia SystemUI", "pkill -TERM com.android.systemui", true, "Riavviare SystemUI?"),
        ShellCommand("Forza luminosita 50%", "settings put system screen_brightness 128", true)
    )

    override suspend fun listCommands(): List<ShellCommand> {
        val rootAvailable = canUseRootShell()
        if (rootAvailable) return commands
        return commands.map { command ->
            if (!command.requiresRoot) return@map command
            command.copy(
                supported = false,
                unsupportedReason = "Il device selezionato non espone root via su, quindi questo comando non e disponibile in modalita adb-only."
            )
        }
    }

    override suspend fun execute(command: ShellCommand): ShellCommandResult = withContext(Dispatchers.IO) {
        if (!command.supported) {
            return@withContext ShellCommandResult(
                success = false,
                stdout = "",
                stderr = command.unsupportedReason ?: "Comando non supportato sul device selezionato",
                code = 126
            )
        }
        runCatching {
            val stdout = shell(command.command, asRoot = command.requiresRoot)
            ShellCommandResult(
                success = true,
                stdout = stdout,
                stderr = "",
                code = 0
            )
        }.getOrElse { error ->
            ShellCommandResult(
                success = false,
                stdout = "",
                stderr = error.message ?: "Errore esecuzione comando",
                code = -1
            )
        }
    }

    private suspend fun canUseRootShell(): Boolean {
        cache.rootShellAvailable?.let { return it }
        val whichSu = runCatching { shell("which su") }.getOrDefault("")
        val suId = runCatching { shell("id", asRoot = true) }.getOrDefault("")
        val available = whichSu.isNotBlank() && suId.contains("uid=0")
        cache.rootShellAvailable = available
        return available
    }
}
