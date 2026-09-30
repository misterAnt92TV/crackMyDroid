package com.crackmydroid.shared.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppManifestSecurityTest {
    @Test
    fun analyzesDebuggableAppAsCritical() {
        val info = AppManifestSecurity(
            appLabel = "TestApp",
            packageName = "com.test",
            debuggable = true,
            allowBackup = false
        )

        val report = ManifestSecurityAnalyzer.analyze(info)

        assertEquals(ManifestRiskLevel.Critical, report.level)
        assertTrue(report.findings.any { it.title.contains("debuggable") })
    }

    @Test
    fun analyzesBackupWithoutAgentAsHigh() {
        val info = AppManifestSecurity(
            appLabel = "TestApp",
            packageName = "com.test",
            debuggable = false,
            allowBackup = true,
            hasCustomBackupAgent = false
        )

        val report = ManifestSecurityAnalyzer.analyze(info)

        assertEquals(ManifestRiskLevel.High, report.level)
        assertTrue(report.findings.any { it.title.contains("Backup") })
    }

    @Test
    fun analyzesExportedProviderWithoutPermissionAsCritical() {
        val info = AppManifestSecurity(
            appLabel = "TestApp",
            packageName = "com.test",
            debuggable = false,
            allowBackup = false,
            exposedComponents = listOf(
                ExposedComponentInfo(
                    type = AppComponentType.PROVIDER,
                    name = "com.test.MyProvider",
                    exported = true,
                    permission = null
                )
            )
        )

        val report = ManifestSecurityAnalyzer.analyze(info)

        assertEquals(ManifestRiskLevel.Critical, report.level)
        assertTrue(report.findings.any { it.title.contains("provider") })
    }

    @Test
    fun analyzesProtectedExportedComponentAsLowRisk() {
        val info = AppManifestSecurity(
            appLabel = "TestApp",
            packageName = "com.test",
            debuggable = false,
            allowBackup = false,
            exposedComponents = listOf(
                ExposedComponentInfo(
                    type = AppComponentType.SERVICE,
                    name = "com.test.MyService",
                    exported = true,
                    permission = "android.permission.INTERNET"
                )
            )
        )

        val report = ManifestSecurityAnalyzer.analyze(info)

        assertEquals(ManifestRiskLevel.Low, report.level)
    }

    @Test
    fun formatsRiskLevelItalian() {
        assertEquals("CRITICA", ManifestSecurityAnalyzer.formatLevel(ManifestRiskLevel.Critical))
        assertEquals("ALTA", ManifestSecurityAnalyzer.formatLevel(ManifestRiskLevel.High))
        assertEquals("MEDIA", ManifestSecurityAnalyzer.formatLevel(ManifestRiskLevel.Medium))
        assertEquals("BASSA", ManifestSecurityAnalyzer.formatLevel(ManifestRiskLevel.Low))
    }

    @Test
    fun calculatesCorrectScore() {
        val info = AppManifestSecurity(
            appLabel = "TestApp",
            packageName = "com.test",
            debuggable = true,
            allowBackup = true,
            hasCustomBackupAgent = false,
            usesCleartextTraffic = true,
            debugCertSigned = true
        )

        val report = ManifestSecurityAnalyzer.analyze(info)

        // Critical (5) + High (3) + Medium (1) + High (3) = 12
        assertEquals(12, report.score)
        assertTrue(report.findings.size >= 4)
    }
}
