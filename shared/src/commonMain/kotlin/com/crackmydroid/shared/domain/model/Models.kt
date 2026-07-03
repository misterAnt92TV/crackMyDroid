package com.crackmydroid.shared.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ActivityEntry(
    val label: String,
    val appLabel: String,
    val packageName: String,
    val activityName: String,
    val launchableViaShell: Boolean = true,
    val launchabilityReason: String? = null,
    val launchContextHint: String? = null,
    val launchIntentAction: String? = null,
    val launchIntentData: String? = null,
    val launchIntentMimeType: String? = null
)

@Serializable
data class RootStatus(
    val isRooted: Boolean,
    val details: String
)

@Serializable
data class PlayIntegrityResult(
    val basicIntegrity: Boolean? = null,
    val deviceIntegrity: Boolean? = null,
    val details: String
)

@Serializable
data class DeviceInfo(
    val manufacturer: String,
    val model: String,
    val brand: String? = null,
    val device: String? = null,
    val product: String? = null,
    val androidVersion: String,
    val securityPatch: String,
    val buildId: String? = null,
    val buildDisplay: String? = null,
    val buildFingerprint: String? = null,
    val radio: String? = null,
    val kernelVersion: String? = null,
    val hardware: String,
    val bootloader: String,
    val batteryLevel: Int?,
    val networkType: String,
    val totalMemory: Long?,
    val freeMemory: Long?,
    val screenResolution: String? = null,
    val densityDpi: Int? = null,
    val adbEnabled: Boolean = false,
    val developerOptions: Boolean = false,
    val appDebuggable: Boolean = false,
    val imei: String? = null,
    val serial: String? = null,
    val systemPropSource: String? = null,
    val systemPropDump: String? = null
)

data class ShellCommand(
    val title: String,
    val command: String,
    val requiresRoot: Boolean,
    val confirmation: String? = null,
    val supported: Boolean = true,
    val unsupportedReason: String? = null
)

data class ShellCommandResult(
    val success: Boolean,
    val stdout: String,
    val stderr: String,
    val code: Int
)

enum class AppTheme { LIGHT, DARK, SYSTEM }

@Serializable
data class AppPermissionEntry(
    val appLabel: String,
    val packageName: String,
    val permissions: List<String>
)

@Serializable
data class InstalledAppEntry(
    val appLabel: String,
    val packageName: String,
    val sourcePath: String
)

@Serializable
data class ConnectedDevice(
    val serial: String,
    val state: String,
    val model: String? = null,
    val product: String? = null,
    val device: String? = null,
    val transportId: String? = null
)
