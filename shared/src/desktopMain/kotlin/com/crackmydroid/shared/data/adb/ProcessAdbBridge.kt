package com.crackmydroid.shared.data.adb

import com.crackmydroid.shared.domain.model.ConnectedDevice
import com.crackmydroid.shared.domain.repository.AdbBridge
import com.crackmydroid.shared.util.runSuspendCatching
import java.io.File
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withContext

class ProcessAdbBridge : AdbBridge {
    override suspend fun resolveBinary(configuredPath: String?): Result<String> = runSuspendCatching {
        resolveBinaryPath(configuredPath)
    }

    override suspend fun listDevices(configuredPath: String?): Result<List<ConnectedDevice>> = runSuspendCatching {
        val adb = resolveBinaryPath(configuredPath)
        val output = runCommand(listOf(adb, "devices", "-l"))
        ensureSuccess(output, "Impossibile leggere i device ADB")
        AdbParsers.parseAdbDevices(output.stdout)
    }

    override suspend fun shell(
        serial: String,
        command: String,
        configuredPath: String?,
        asRoot: Boolean,
    ): Result<String> = shellWithTimeout(serial, command, configuredPath, asRoot)

    override suspend fun shellWithTimeout(
        serial: String,
        command: String,
        configuredPath: String?,
        asRoot: Boolean,
        timeoutMs: Long,
    ): Result<String> = runSuspendCatching {
        val adb = resolveBinaryPath(configuredPath)
        val shellArgs = if (asRoot) {
            listOf("shell", "su", "-c", command)
        } else {
            listOf("shell", "sh", "-c", command)
        }
        val output = runCommand(listOf(adb, "-s", serial) + shellArgs, timeoutMs)
        ensureSuccess(output, "Comando shell fallito")
        output.stdout.trim()
    }

    override suspend fun pull(
        serial: String,
        remotePath: String,
        localPath: String,
        configuredPath: String?,
    ): Result<String> = runSuspendCatching {
        val adb = resolveBinaryPath(configuredPath)
        File(localPath).parentFile?.mkdirs()
        val output = runCommand(listOf(adb, "-s", serial, "pull", remotePath, localPath))
        ensureSuccess(output, "Copia file da device fallita")
        localPath
    }

    override suspend fun logcat(serial: String, configuredPath: String?, lines: Int): Result<List<String>> =
        runSuspendCatching {
            val adb = resolveBinaryPath(configuredPath)
            val output = runCommand(
                listOf(
                    adb,
                    "-s",
                    serial,
                    "logcat",
                    "-d",
                    "-t",
                    lines.toString(),
                    "-v",
                    "time",
                ),
            )
            ensureSuccess(output, "Lettura logcat fallita")
            output.stdout.lines().filter { it.isNotBlank() }
        }

    private fun resolveBinaryPath(configuredPath: String?): String {
        val explicit = configuredPath?.trim().orEmpty()
        if (explicit.isNotBlank()) {
            val file = File(explicit)
            require(file.isFile && file.canExecute()) {
                "adb non valido o non eseguibile in $explicit"
            }
            return file.absolutePath
        }

        val pathEntries = (System.getenv("PATH") ?: "")
            .split(File.pathSeparatorChar)
            .filter { it.isNotBlank() }

        val candidate = pathEntries
            .asSequence()
            .map { File(it, "adb") }
            .firstOrNull { it.exists() && it.canExecute() }

        requireNotNull(candidate) { "adb non trovato nel PATH. Specifica un percorso valido." }
        return candidate.absolutePath
    }

    private suspend fun runCommand(args: List<String>, timeoutMs: Long = 60_000L): CommandOutput =
        withContext(Dispatchers.IO) {
            coroutineScope {
                val process = ProcessBuilder(args).start()
                try {
                    val stdout = async { process.inputStream.bufferedReader().use { it.readText() } }
                    val stderr = async { process.errorStream.bufferedReader().use { it.readText() } }
                    if (!runInterruptible { process.waitFor(timeoutMs, TimeUnit.MILLISECONDS) }) {
                        process.destroyForcibly()
                        error("Timeout eseguendo: ${args.joinToString(" ")}")
                    }
                    CommandOutput(
                        exitCode = process.exitValue(),
                        stdout = stdout.await().trim(),
                        stderr = stderr.await().trim(),
                    )
                } finally {
                    if (process.isAlive) process.destroyForcibly()
                }
            }
        }

    private fun ensureSuccess(output: CommandOutput, message: String) {
        if (output.exitCode == 0) return
        val detail = output.stderr.ifBlank { output.stdout }.ifBlank { "exit=${output.exitCode}" }
        error("$message: $detail")
    }

    private data class CommandOutput(val exitCode: Int, val stdout: String, val stderr: String)
}
