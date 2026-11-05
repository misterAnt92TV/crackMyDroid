package com.crackmydroid.shared.domain.usecase.permissions

import com.crackmydroid.shared.domain.model.app.AppPermissionEntry
import com.crackmydroid.shared.domain.model.PermissionRiskFinding
import com.crackmydroid.shared.domain.model.PermissionRiskLevel
import com.crackmydroid.shared.domain.model.PermissionRiskReport
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BuildPermissionsReportUseCaseTest {
    private val analyzer = AnalyzePermissionRisksUseCase()
    private val useCase = BuildPermissionsReportUseCase(analyzer)

    @Test
    fun invoke_emptyApps_returnsNoDataReport() {
        val report = useCase(emptyList(), emptyList())

        assertTrue(report.contains("Totale app analizzate: 0"))
        assertTrue(report.contains("Nessun dato disponibile."))
    }

    @Test
    fun invoke_riskReports_sortsByScoreAndDescribesSafeApps() {
        val safe = AppPermissionEntry("Alpha", "pkg.alpha", emptyList())
        val risky = AppPermissionEntry("Zulu", "pkg.zulu", listOf("android.permission.CAMERA"))
        val report = useCase(listOf(safe, risky), listOf(riskReport(risky)))

        assertTrue(report.indexOf("1) Zulu") < report.indexOf("2) Alpha"))
        assertTrue(report.contains("Analisi rischio: score 70"))
        assertTrue(report.contains("nessun comportamento sensibile rilevato"))
    }

    @Test
    fun single_andFileName_riskAndUnsafePackage_returnDetailedSafeOutput() {
        val app = AppPermissionEntry("Demo", "com.demo/app unsafe", listOf("B", "A"))
        val report = useCase.single(app, riskReport(app))

        assertTrue(report.contains("1. [CRITICO] CAMERA"))
        assertTrue(report.indexOf("1. A") < report.indexOf("2. B"))
        assertEquals("permissions_com_demo_app_unsafe.txt", useCase.singleFileName(app.packageName))
    }

    @Test
    fun `summary totals all apps permissions and severities and sorts ties by label`() {
        val zulu = AppPermissionEntry("Zulu", "pkg.z", listOf("Z", "A"))
        val alpha = AppPermissionEntry("alpha", "pkg.a", emptyList())
        val report = useCase(
            listOf(zulu, alpha),
            listOf(
                riskReport(zulu).copy(highCount = 2, mediumCount = 3),
                riskReport(alpha).copy(criticalCount = 2, highCount = 1, mediumCount = 1)
            )
        )
        assertTrue(report.contains("Totale app analizzate: 2"))
        assertTrue(report.contains("Totale permessi trovati: 2"))
        assertTrue(report.contains("App con rischi rilevati: 2"))
        assertTrue(report.contains("Critici: 3 | Alti: 3 | Medi: 4"))
        assertTrue(report.contains("1) alpha"))
        assertTrue(report.contains("2) Zulu"))
        assertTrue(report.contains("- Nessun permesso dichiarato"))
        assertTrue(report.indexOf("- A") < report.indexOf("- Z"))
        assertTrue(report.contains("- [CRITICO] CAMERA"))
        assertTrue(report.contains("camera access"))
    }

    @Test
    fun `single report without risk or permissions explains empty sections`() {
        val app = AppPermissionEntry("Safe", "pkg.safe", emptyList())
        val report = useCase.single(app, null)
        assertTrue(report.contains("App: Safe"))
        assertTrue(report.contains("Package: pkg.safe"))
        assertTrue(report.contains("Totale permessi: 0"))
        assertTrue(report.contains("Nessun permesso pericoloso/malevolo"))
        assertTrue(report.contains("Nessun permesso dichiarato."))
        assertEquals("permissions_.txt", useCase.singleFileName(""))
        assertEquals("permissions_com_demo-app_v2.txt", useCase.singleFileName("com.demo-app_v2"))
    }

    private fun riskReport(app: AppPermissionEntry) = PermissionRiskReport(
        appLabel = app.appLabel,
        packageName = app.packageName,
        findings = listOf(
            PermissionRiskFinding("CAMERA", "CAMERA", PermissionRiskLevel.Critical, "camera access")
        ),
        score = 70,
        criticalCount = 1,
        highCount = 0,
        mediumCount = 0
    )
}
