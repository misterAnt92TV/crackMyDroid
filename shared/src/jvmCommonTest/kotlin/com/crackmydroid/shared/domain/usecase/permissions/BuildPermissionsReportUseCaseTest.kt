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
