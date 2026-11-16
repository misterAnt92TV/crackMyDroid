package com.crackmydroid.shared.domain.model

import com.crackmydroid.shared.domain.model.app.AppComponentType
import com.crackmydroid.shared.domain.model.app.AppManifestSecurity
import com.crackmydroid.shared.domain.model.app.ExposedComponentInfo
import com.crackmydroid.shared.domain.model.app.ManifestRiskLevel
import com.crackmydroid.shared.domain.model.app.ManifestSecurityAnalyzer
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
            allowBackup = false,
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
            hasCustomBackupAgent = false,
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
                    permission = null,
                ),
            ),
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
                    permission = "android.permission.INTERNET",
                ),
            ),
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
            debugCertSigned = true,
        )

        val report = ManifestSecurityAnalyzer.analyze(info)

        // Critical (5) + High (3) + Medium (1) + High (3) = 12
        assertEquals(12, report.score)
        assertTrue(report.findings.size >= 4)
    }

    @Test
    fun analyzesExportedActivityWithoutPermissionAsHigh() {
        val info = AppManifestSecurity(
            appLabel = "TestApp",
            packageName = "com.test",
            debuggable = false,
            allowBackup = false,
            exposedComponents = listOf(
                ExposedComponentInfo(
                    type = AppComponentType.ACTIVITY,
                    name = "com.test.DeepLinkActivity",
                    exported = true,
                    permission = null,
                ),
            ),
        )

        val report = ManifestSecurityAnalyzer.analyze(info)

        assertEquals(ManifestRiskLevel.High, report.level)
        val finding = report.findings.first { it.title.contains("Activity") }
        assertEquals(ManifestRiskLevel.High, finding.level)
        assertTrue(finding.detail.contains("DeepLinkActivity"))
        assertTrue(finding.remediation != null)
    }

    @Test
    fun analyzesObsoleteTargetSdkBelow28AsHigh() {
        val info = AppManifestSecurity(
            appLabel = "TestApp",
            packageName = "com.test",
            debuggable = false,
            allowBackup = false,
            targetSdkVersion = 26,
        )

        val report = ManifestSecurityAnalyzer.analyze(info)

        assertEquals(ManifestRiskLevel.High, report.level)
        val finding = report.findings.first { it.title.contains("Target SDK") }
        assertTrue(finding.detail.contains("cleartext"))
    }

    @Test
    fun analyzesTargetSdkBelow31AsMedium() {
        val info = AppManifestSecurity(
            appLabel = "TestApp",
            packageName = "com.test",
            debuggable = false,
            allowBackup = false,
            targetSdkVersion = 30,
        )

        val report = ManifestSecurityAnalyzer.analyze(info)

        assertEquals(ManifestRiskLevel.Medium, report.level)
        val finding = report.findings.first { it.title.contains("Target SDK") }
        assertTrue(finding.detail.contains("exported"))
    }

    @Test
    fun analyzesBackupWithCustomAgentAsMedium() {
        val info = AppManifestSecurity(
            appLabel = "TestApp",
            packageName = "com.test",
            debuggable = false,
            allowBackup = true,
            hasCustomBackupAgent = true,
            targetSdkVersion = 34,
        )

        val report = ManifestSecurityAnalyzer.analyze(info)

        assertEquals(ManifestRiskLevel.Medium, report.level)
        assertTrue(report.findings.any { it.title.contains("BackupAgent") })
    }

    @Test
    fun analyzesHardenedAppWithZeroRisks() {
        val info = AppManifestSecurity(
            appLabel = "HardenedApp",
            packageName = "com.hardened",
            debuggable = false,
            allowBackup = false,
            usesCleartextTraffic = false,
            testOnly = false,
            targetSdkVersion = 35,
            minSdkVersion = 26,
            debugCertSigned = false,
            exposedComponents = listOf(
                ExposedComponentInfo(
                    type = AppComponentType.ACTIVITY,
                    name = "com.hardened.MainActivity",
                    exported = true,
                    permission = "com.hardened.permission.SECURE_ACCESS",
                ),
            ),
        )

        val report = ManifestSecurityAnalyzer.analyze(info)

        assertEquals(ManifestRiskLevel.Low, report.level)
        assertEquals(0, report.score)
        assertTrue(report.findings.isEmpty())
    }

    @Test
    fun findingsIncludeEvidenceAndRemediation() {
        val info = AppManifestSecurity(
            appLabel = "VulnerableApp",
            packageName = "com.vuln",
            debuggable = true,
            allowBackup = true,
            usesCleartextTraffic = true,
            targetSdkVersion = 25,
        )

        val report = ManifestSecurityAnalyzer.analyze(info)

        assertTrue(report.findings.isNotEmpty())
        for (finding in report.findings) {
            assertTrue(finding.evidence?.isNotBlank() == true, "Finding '${finding.title}' missing evidence")
            assertTrue(finding.remediation?.isNotBlank() == true, "Finding '${finding.title}' missing remediation")
        }
    }
}
