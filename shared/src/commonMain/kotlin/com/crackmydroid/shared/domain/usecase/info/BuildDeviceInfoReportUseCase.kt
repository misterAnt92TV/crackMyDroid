package com.crackmydroid.shared.domain.usecase.info

import com.crackmydroid.shared.domain.model.DeviceInfo
import com.crackmydroid.shared.domain.model.RootStatus

class BuildDeviceInfoReportUseCase {
    operator fun invoke(info: DeviceInfo, root: RootStatus?): String = buildString {
        appendLine("Device info")
        appendLine("Manufacturer: ${info.manufacturer}")
        appendLine("Model: ${info.model}")
        appendLine("Brand: ${info.brand}")
        appendLine("Device: ${info.device}")
        appendLine("Product: ${info.product}")
        appendLine("Android: ${info.androidVersion}")
        appendLine("Patch: ${info.securityPatch}")
        appendLine("Build ID: ${info.buildId}")
        appendLine("Build display: ${info.buildDisplay}")
        appendLine("Fingerprint: ${info.buildFingerprint}")
        appendLine("Radio: ${info.radio}")
        appendLine("Kernel: ${info.kernelVersion}")
        appendLine("Hardware: ${info.hardware}")
        appendLine("Bootloader: ${info.bootloader}")
        appendLine("Battery: ${info.batteryLevel ?: -1}%")
        appendLine("Network: ${info.networkType}")
        appendLine("Total RAM: ${info.totalMemory}")
        appendLine("Free RAM: ${info.freeMemory}")
        appendLine("Screen: ${info.screenResolution} (${info.densityDpi} dpi)")
        appendLine("ADB enabled: ${info.adbEnabled}")
        appendLine("Developer options: ${info.developerOptions}")
        appendLine("App debuggable: ${info.appDebuggable}")
        root?.let { appendLine("Rooted: ${it.isRooted}"); appendLine("Root details: ${it.details}") }
        appendLine("IMEI: ${info.imei}")
        appendLine("Serial: ${info.serial}")
        if (!info.systemPropDump.isNullOrBlank()) {
            appendLine(); appendLine("System properties source: ${info.systemPropSource ?: "n/a"}")
            appendLine("System properties dump:"); appendLine(info.systemPropDump)
        }
    }
}
