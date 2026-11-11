package com.crackmydroid.shared.domain.repository

import com.crackmydroid.shared.domain.model.ConnectedDevice

interface AdbBridge {
    suspend fun resolveBinary(configuredPath: String? = null): Result<String>
    suspend fun listDevices(configuredPath: String? = null): Result<List<ConnectedDevice>>
    suspend fun shell(
        serial: String,
        command: String,
        configuredPath: String? = null,
        asRoot: Boolean = false,
    ): Result<String>

    suspend fun shellWithTimeout(
        serial: String,
        command: String,
        configuredPath: String? = null,
        asRoot: Boolean = false,
        timeoutMs: Long = 60_000L,
    ): Result<String> = shell(serial, command, configuredPath, asRoot)

    suspend fun pull(
        serial: String,
        remotePath: String,
        localPath: String,
        configuredPath: String? = null,
    ): Result<String>

    suspend fun logcat(serial: String, configuredPath: String? = null, lines: Int = 1500): Result<List<String>>
}
