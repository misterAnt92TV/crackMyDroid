package com.crackmydroid.shared.domain.usecase.info

import com.crackmydroid.shared.domain.model.DeviceInfo
import com.crackmydroid.shared.domain.model.ReportLabel
import com.crackmydroid.shared.domain.model.RootStatus
import com.crackmydroid.shared.domain.usecase.UseCaseTestDispatchers
import com.crackmydroid.shared.i18n.UseCaseTexts
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

class BuildDeviceInfoReportUseCaseTest {
    private val dispatchers = UseCaseTestDispatchers()
    private val useCase = BuildDeviceInfoReportUseCase(dispatchers)

    @Test
    fun `device report localizes labels and booleans while retaining technical values`() =
        runTest(dispatchers.default) {
            val info = DeviceInfo(
                manufacturer = "Demo manufacturer",
                model = "Model X",
                androidVersion = "16",
                batteryLevel = 75,
                totalMemory = 4096,
                freeMemory = 2048,
                adbEnabled = true,
                developerOptions = false,
                appDebuggable = true,
                imei = "123456789",
                systemPropSource = "build fingerprint",
                systemPropDump = "ro.build.version.release=16"
            )
            val root = RootStatus(isRooted = false, details = "su unavailable")

            for (language in listOf("en", "it", "de", "es", "fr")) {
                val texts = UseCaseTexts(language)
                val report = useCase(info, root, texts).getOrThrow()
                assertTrue(report.startsWith(texts.label(ReportLabel.DEVICE_INFO_TITLE)))
                assertTrue(report.contains("${texts.label(ReportLabel.MANUFACTURER)}: Demo manufacturer"))
                assertTrue(report.contains("${texts.label(ReportLabel.MODEL)}: Model X"))
                assertTrue(report.contains("${texts.label(ReportLabel.ADB_ENABLED)}: ${texts.boolean(true)}"))
                assertTrue(report.contains("${texts.label(ReportLabel.DEVELOPER_OPTIONS)}: ${texts.boolean(false)}"))
                assertTrue(report.contains("${texts.label(ReportLabel.APP_DEBUGGABLE)}: ${texts.boolean(true)}"))
                assertTrue(report.contains("${texts.label(ReportLabel.ROOTED)}: ${texts.boolean(false)}"))
                assertTrue(report.contains("IMEI: 123456789"))
                assertTrue(report.contains("ro.build.version.release=16"))
            }
        }
}
