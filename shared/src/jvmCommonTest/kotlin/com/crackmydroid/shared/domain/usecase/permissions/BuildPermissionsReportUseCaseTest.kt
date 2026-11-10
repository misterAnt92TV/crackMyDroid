package com.crackmydroid.shared.domain.usecase.permissions

import com.crackmydroid.shared.domain.model.PermissionFindingKind
import com.crackmydroid.shared.domain.model.PermissionRiskFinding
import com.crackmydroid.shared.domain.model.PermissionRiskLevel
import com.crackmydroid.shared.domain.model.PermissionRiskReport
import com.crackmydroid.shared.domain.model.app.AppPermissionEntry
import com.crackmydroid.shared.domain.usecase.UseCaseTestDispatchers
import com.crackmydroid.shared.i18n.usecase.UseCaseTexts
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BuildPermissionsReportUseCaseTest {
    private val dispatchers = UseCaseTestDispatchers()
    private val analyzer = AnalyzePermissionRisksUseCase(dispatchers)
    private val useCase = BuildPermissionsReportUseCase(analyzer, dispatchers)
    private val text = UseCaseTexts("en")

    @Test
    fun invoke_emptyApps_returnsNoDataReport() = runTest(dispatchers.default) {
        val report = useCase(emptyList(), emptyList(), text).getOrThrow()

        assertTrue(report.contains("0 apps analyzed"))
        assertTrue(report.contains("No data available."))
    }

    @Test
    fun invoke_riskReports_sortsByScoreAndDescribesSafeApps() = runTest(dispatchers.default) {
        val safe = AppPermissionEntry("Alpha", "pkg.alpha", emptyList())
        val risky = AppPermissionEntry("Zulu", "pkg.zulu", listOf("android.permission.CAMERA"))
        val report = useCase(listOf(safe, risky), listOf(riskReport(risky)), text).getOrThrow()

        assertTrue(report.indexOf("1) Zulu") < report.indexOf("2) Alpha"))
        assertTrue(report.contains("Risk analysis: score 70"))
        assertTrue(report.contains("no sensitive behavior detected"))
    }

    @Test
    fun single_andFileName_riskAndUnsafePackage_returnDetailedSafeOutput() = runTest(dispatchers.default) {
        val app = AppPermissionEntry("Demo", "com.demo/app unsafe", listOf("B", "A"))
        val report = useCase.single(app, riskReport(app), text).getOrThrow()

        assertTrue(report.contains("1. [CRITICAL] CAMERA"))
        assertTrue(report.indexOf("1. A") < report.indexOf("2. B"))
        assertEquals("permissions_com_demo_app_unsafe.txt", useCase.singleFileName(app.packageName))
    }

    @Test
    fun `summary totals all apps permissions and severities and sorts ties by label`() = runTest(dispatchers.default) {
        val zulu = AppPermissionEntry("Zulu", "pkg.z", listOf("Z", "A"))
        val alpha = AppPermissionEntry("alpha", "pkg.a", emptyList())
        val report = useCase(
            listOf(zulu, alpha),
            listOf(
                riskReport(zulu).copy(highCount = 2, mediumCount = 3),
                riskReport(alpha).copy(criticalCount = 2, highCount = 1, mediumCount = 1),
            ),
            text,
        ).getOrThrow()
        assertTrue(report.contains("2 apps analyzed"))
        assertTrue(report.contains("2 permissions found"))
        assertTrue(report.contains("2 apps with detected risks"))
        assertTrue(report.contains("CRITICAL: 3 | HIGH: 3 | MEDIUM: 4"))
        assertTrue(report.contains("1) alpha"))
        assertTrue(report.contains("2) Zulu"))
        assertTrue(report.contains("- No permissions declared"))
        assertTrue(report.indexOf("- A") < report.indexOf("- Z"))
        assertTrue(report.contains("- [CRITICAL] CAMERA"))
        assertTrue(report.contains("Microphone or camera access"))
    }

    @Test
    fun `single report without risk or permissions explains empty sections`() = runTest(dispatchers.default) {
        val app = AppPermissionEntry("Safe", "pkg.safe", emptyList())
        val report = useCase.single(app, null, text).getOrThrow()
        assertTrue(report.contains("App: Safe"))
        assertTrue(report.contains("Package: pkg.safe"))
        assertTrue(report.contains("Declared permissions: 0"))
        assertTrue(report.contains("No risky or malicious permissions"))
        assertTrue(report.contains("No permissions declared"))
        assertEquals("permissions_.txt", useCase.singleFileName(""))
        assertEquals("permissions_com_demo-app_v2.txt", useCase.singleFileName("com.demo-app_v2"))
    }

    @Test
    fun `complete and single reports render semantic findings in each supported language`() =
        runTest(dispatchers.default) {
            val app = AppPermissionEntry("Demo", "pkg.demo", listOf("CAMERA"))
            val report = riskReport(app)
            for (language in listOf("en", "it", "de", "es", "fr")) {
                val texts = UseCaseTexts(language)
                val complete = useCase(listOf(app), listOf(report), texts).getOrThrow()
                val single = useCase.single(app, report, texts).getOrThrow()
                val translatedReason = texts.permissionReason(report.findings.single())
                assertTrue(complete.startsWith(texts.label(com.crackmydroid.shared.domain.model.ReportLabel.PERMISSIONS_REPORT_TITLE)))
                assertTrue(single.startsWith(texts.label(com.crackmydroid.shared.domain.model.ReportLabel.SINGLE_PERMISSIONS_REPORT_TITLE)))
                assertTrue(complete.contains("CAMERA"))
                assertTrue(single.contains("CAMERA"))
                assertTrue(complete.contains(translatedReason))
                assertTrue(single.contains(translatedReason))
            }
        }

    private fun riskReport(app: AppPermissionEntry) = PermissionRiskReport(
        appLabel = app.appLabel,
        packageName = app.packageName,
        findings = listOf(
            PermissionRiskFinding(
                key = "CAMERA",
                kind = PermissionFindingKind.MICROPHONE_CAMERA_ACCESS,
                level = PermissionRiskLevel.Critical,
            ),
        ),
        score = 70,
        criticalCount = 1,
        highCount = 0,
        mediumCount = 0,
    )
}
