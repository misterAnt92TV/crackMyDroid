package com.crackmydroid.shared.data.adb

import com.crackmydroid.shared.domain.model.ConnectedDevice
import com.crackmydroid.shared.domain.repository.AdbBridge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.TimeUnit

class ProcessAdbBridge : AdbBridge {
    override suspend fun resolveBinary(configuredPath: String?): Result<String> = runCatching {
        resolveBinaryPath(configuredPath)
    }

    override suspend fun listDevices(configuredPath: String?): Result<List<ConnectedDevice>> = runCatching {
        val adb = resolveBinaryPath(configuredPath)
        val output = runCommand(listOf(adb, "devices", "-l"))
        ensureSuccess(output, "Impossibile leggere i device ADB")
        AdbParsers.parseAdbDevices(output.stdout)
    }

    override suspend fun shell(
        serial: String,
        command: String,
        configuredPath: String?,
        asRoot: Boolean
    ): Result<String> = runCatching {
        val adb = resolveBinaryPath(configuredPath)
        val shellArgs = if (asRoot) {
            listOf("shell", "su", "-c", command)
        } else {
            listOf("shell", "sh", "-c", command)
        }
        val output = runCommand(listOf(adb, "-s", serial) + shellArgs)
        ensureSuccess(output, "Comando shell fallito")
        output.stdout.trim()
    }

    override suspend fun pull(
        serial: String,
        remotePath: String,
        localPath: String,
        configuredPath: String?
    ): Result<String> = runCatching {
        val adb = resolveBinaryPath(configuredPath)
        File(localPath).parentFile?.mkdirs()
        val output = runCommand(listOf(adb, "-s", serial, "pull", remotePath, localPath))
        ensureSuccess(output, "Copia file da device fallita")
        localPath
    }

    override suspend fun logcat(
        serial: String,
        configuredPath: String?,
        lines: Int
    ): Result<List<String>> = runCatching {
        val adb = resolveBinaryPath(configuredPath)
        val output = runCommand(listOf(adb, "-s", serial, "logcat", "-d", "-t", lines.toString(), "-v", "time"))
        ensureSuccess(output, "Lettura logcat fallita")
        output.stdout.lines().filter { it.isNotBlank() }
    }

    private fun resolveBinaryPath(configuredPath: String?): String {
        val explicit = configuredPath?.trim().orEmpty()
        if (explicit.isNotBlank()) {
            val file = File(explicit)
            require(file.exists()) { "adb non trovato in $explicit" }
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

    private suspend fun runCommand(args: List<String>): CommandOutput = withContext(Dispatchers.IO) {
        coroutineScope {
            val process = ProcessBuilder(args).start()
            val stdout = async { process.inputStream.bufferedReader().use { it.readText() } }
            val stderr = async { process.errorStream.bufferedReader().use { it.readText() } }
            if (!process.waitFor(60, TimeUnit.SECONDS)) {
                process.destroyForcibly()
                error("Timeout eseguendo: ${args.joinToString(" ")}")
            }
            CommandOutput(
                exitCode = process.exitValue(),
                stdout = stdout.await().trim(),
                stderr = stderr.await().trim()
            )
        }
    }

    private fun ensureSuccess(output: CommandOutput, message: String) {
        if (output.exitCode == 0) return
        val detail = output.stderr.ifBlank { output.stdout }.ifBlank { "exit=${output.exitCode}" }
        error("$message: $detail")
    }

    private data class CommandOutput(
        val exitCode: Int,
        val stdout: String,
        val stderr: String
    )
}
