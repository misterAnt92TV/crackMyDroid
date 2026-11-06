package com.crackmydroid.shared.domain.usecase.info

import com.crackmydroid.shared.domain.model.DeviceInfo
import com.crackmydroid.shared.domain.model.RootStatus
import com.crackmydroid.shared.domain.model.ReportLabel
import com.crackmydroid.shared.domain.model.UseCaseReportText
import co.touchlab.kermit.Logger
import com.crackmydroid.shared.AppLogger
import com.crackmydroid.shared.domain.usecase.BaseUseCase
import com.crackmydroid.shared.util.AppDispatchers
import com.crackmydroid.shared.util.DefaultAppDispatchers

class BuildDeviceInfoReportUseCase(
    dispatchers: AppDispatchers = DefaultAppDispatchers,
    logger: Logger = AppLogger.logger
) : BaseUseCase(TAG, dispatcher = dispatchers.default, logger = logger) {
    private companion object {
        private const val TAG = "BuildDeviceInfoReportUseCase"
    }

    suspend operator fun invoke(
        info: DeviceInfo,
        root: RootStatus?,
        text: UseCaseReportText
    ): Result<String> = execute {
        buildString {
            appendLine(text.label(ReportLabel.DEVICE_INFO_TITLE))
            appendLine("${text.label(ReportLabel.MANUFACTURER)}: ${info.manufacturer}")
            appendLine("${text.label(ReportLabel.MODEL)}: ${info.model}")
            appendLine("${text.label(ReportLabel.BRAND)}: ${info.brand}")
            appendLine("${text.label(ReportLabel.DEVICE)}: ${info.device}")
            appendLine("${text.label(ReportLabel.PRODUCT)}: ${info.product}")
            appendLine("${text.label(ReportLabel.ANDROID)}: ${info.androidVersion}")
            appendLine("${text.label(ReportLabel.SECURITY_PATCH)}: ${info.securityPatch}")
            appendLine("${text.label(ReportLabel.BUILD_ID)}: ${info.buildId}")
            appendLine("${text.label(ReportLabel.BUILD_DISPLAY)}: ${info.buildDisplay}")
            appendLine("${text.label(ReportLabel.FINGERPRINT)}: ${info.buildFingerprint}")
            appendLine("${text.label(ReportLabel.RADIO)}: ${info.radio}")
            appendLine("${text.label(ReportLabel.KERNEL)}: ${info.kernelVersion}")
            appendLine("${text.label(ReportLabel.HARDWARE)}: ${info.hardware}")
            appendLine("${text.label(ReportLabel.BOOTLOADER)}: ${info.bootloader}")
            appendLine("${text.label(ReportLabel.BATTERY)}: ${info.batteryLevel ?: -1}%")
            appendLine("${text.label(ReportLabel.NETWORK)}: ${info.networkType}")
            appendLine("${text.label(ReportLabel.TOTAL_RAM)}: ${info.totalMemory}")
            appendLine("${text.label(ReportLabel.FREE_RAM)}: ${info.freeMemory}")
            appendLine("${text.label(ReportLabel.SCREEN)}: ${info.screenResolution} (${info.densityDpi} dpi)")
            appendLine("${text.label(ReportLabel.ADB_ENABLED)}: ${text.boolean(info.adbEnabled)}")
            appendLine("${text.label(ReportLabel.DEVELOPER_OPTIONS)}: ${text.boolean(info.developerOptions)}")
            appendLine("${text.label(ReportLabel.APP_DEBUGGABLE)}: ${text.boolean(info.appDebuggable)}")
            root?.let {
                appendLine("${text.label(ReportLabel.ROOTED)}: ${text.boolean(it.isRooted)}")
                appendLine("${text.label(ReportLabel.ROOT_DETAILS)}: ${it.details}")
            }
            appendLine("${text.label(ReportLabel.IMEI)}: ${info.imei}")
            appendLine("${text.label(ReportLabel.SERIAL)}: ${info.serial}")
            if (!info.systemPropDump.isNullOrBlank()) {
                appendLine()
                appendLine("${text.label(ReportLabel.SYSTEM_PROPERTIES_SOURCE)}: ${info.systemPropSource ?: "n/a"}")
                appendLine("${text.label(ReportLabel.SYSTEM_PROPERTIES_DUMP)}:")
                appendLine(info.systemPropDump)
            }
        }
    }
}
