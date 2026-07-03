package com.crackmydroid.shared.data.device

import com.crackmydroid.shared.data.DesktopAdbRepositorySupport
import com.crackmydroid.shared.data.DesktopScanCache
import com.crackmydroid.shared.data.adb.AdbParsers
import com.crackmydroid.shared.domain.model.DeviceInfo
import com.crackmydroid.shared.domain.repository.AdbBridge
import com.crackmydroid.shared.domain.repository.DeviceInfoRepository
import com.crackmydroid.shared.domain.repository.DeviceSessionController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DeviceInfoRepositoryDesktop(
    adbBridge: AdbBridge,
    sessionController: DeviceSessionController,
    private val cache: DesktopScanCache
) : DesktopAdbRepositorySupport(adbBridge, sessionController), DeviceInfoRepository {
    override suspend fun getDeviceInfo(): DeviceInfo = withContext(Dispatchers.Default) {
        cache.deviceInfo?.let { return@withContext it }

        val device = selectedDevice()
        val propsText = shell("getprop")
        val props = AdbParsers.parseGetProp(propsText)
        val battery = shell("dumpsys battery")
        val wmSize = shell("wm size")
        val wmDensity = shell("wm density")
        val memInfo = shell("cat /proc/meminfo")
        val kernel = shell("uname -a")
        val adbEnabled = shell("settings get global adb_enabled").trim() == "1"
        val developerOptions = shell("settings get global development_settings_enabled").trim() == "1"

        val (totalMemory, freeMemory) = AdbParsers.parseMemInfo(memInfo)
        val info = DeviceInfo(
            manufacturer = props["ro.product.manufacturer"] ?: props["ro.product.vendor.manufacturer"] ?: "unknown",
            model = props["ro.product.model"] ?: device.model ?: "unknown",
            brand = props["ro.product.brand"],
            device = props["ro.product.device"] ?: device.device,
            product = props["ro.product.name"] ?: device.product,
            androidVersion = props["ro.build.version.release"] ?: "unknown",
            securityPatch = props["ro.build.version.security_patch"] ?: "unknown",
            buildId = props["ro.build.id"],
            buildDisplay = props["ro.build.display.id"],
            buildFingerprint = props["ro.build.fingerprint"],
            radio = props["gsm.version.baseband"] ?: props["ril.sw_ver"],
            kernelVersion = kernel.ifBlank { null },
            hardware = props["ro.hardware"] ?: "n/a",
            bootloader = props["ro.bootloader"] ?: "n/a",
            batteryLevel = AdbParsers.parseBatteryLevel(battery),
            networkType = props["gsm.network.type"] ?: props["wifi.interface"] ?: "unknown",
            totalMemory = totalMemory,
            freeMemory = freeMemory,
            screenResolution = AdbParsers.parseWmSize(wmSize),
            densityDpi = AdbParsers.parseDensity(wmDensity),
            adbEnabled = adbEnabled,
            developerOptions = developerOptions,
            appDebuggable = false,
            imei = null,
            serial = device.serial,
            systemPropSource = "adb:getprop",
            systemPropDump = propsText
        )
        cache.deviceInfo = info
        info
    }
}
