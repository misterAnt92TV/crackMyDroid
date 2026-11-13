package com.crackmydroid.shared.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class DeviceInfo(
    val manufacturer: String? = null,
    val model: String? = null,
    val brand: String? = null,
    val device: String? = null,
    val product: String? = null,
    val androidVersion: String? = null,
    val securityPatch: String? = null,
    val buildId: String? = null,
    val buildDisplay: String? = null,
    val buildFingerprint: String? = null,
    val radio: String? = null,
    val kernelVersion: String? = null,
    val hardware: String? = null,
    val bootloader: String? = null,
    val batteryLevel: Int?,
    val networkType: String? = null,
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
    val systemPropDump: String? = null,
)
