package com.misterant92tv.crackmydroid

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for [SecurityReport] risk scoring and findings logic.
 *
 * These tests exercise pure Kotlin logic without any Android framework dependency.
 */
class SecurityReportTest {

    private fun buildReport(
        packageName: String = "com.example.test",
        appName: String = "Test App",
        versionName: String? = "1.0",
        versionCode: Long = 1L,
        isDebuggable: Boolean = false,
        allowsBackup: Boolean = false,
        isTestOnly: Boolean = false,
        isSystemApp: Boolean = false,
        minSdkVersion: Int = 26,
        dangerousPermissions: List<String> = emptyList(),
        exportedActivities: List<String> = emptyList(),
        exportedServices: List<String> = emptyList(),
        exportedReceivers: List<String> = emptyList(),
        exportedProviders: List<String> = emptyList(),
        networkSecurityConfigPresent: Boolean = true
    ) = SecurityReport(
        packageName = packageName,
        appName = appName,
        versionName = versionName,
        versionCode = versionCode,
        isDebuggable = isDebuggable,
        allowsBackup = allowsBackup,
        isTestOnly = isTestOnly,
        isSystemApp = isSystemApp,
        minSdkVersion = minSdkVersion,
        dangerousPermissions = dangerousPermissions,
        exportedActivities = exportedActivities,
        exportedServices = exportedServices,
        exportedReceivers = exportedReceivers,
        exportedProviders = exportedProviders,
        networkSecurityConfigPresent = networkSecurityConfigPresent
    )

    // ── Risk score ────────────────────────────────────────────────────────────

    @Test
    fun `score is 0 for a perfectly safe app`() {
        val report = buildReport()
        assertEquals(0, report.riskScore)
    }

    @Test
    fun `debuggable flag adds 30 to score`() {
        val report = buildReport(isDebuggable = true)
        assertEquals(30, report.riskScore)
    }

    @Test
    fun `allowBackup adds 10 to score`() {
        val report = buildReport(allowsBackup = true)
        assertEquals(10, report.riskScore)
    }

    @Test
    fun `testOnly flag adds 10 to score`() {
        val report = buildReport(isTestOnly = true)
        assertEquals(10, report.riskScore)
    }

    @Test
    fun `minSdk below 23 adds 10 to score`() {
        val report = buildReport(minSdkVersion = 16)
        assertEquals(10, report.riskScore)
    }

    @Test
    fun `minSdk of 23 does not add to score`() {
        val report = buildReport(minSdkVersion = 23)
        assertEquals(0, report.riskScore)
    }

    @Test
    fun `minSdk 0 does not add to score`() {
        // 0 means unknown – should not trigger the penalty (range is 1..22)
        val report = buildReport(minSdkVersion = 0)
        assertEquals(0, report.riskScore)
    }

    @Test
    fun `each dangerous permission adds 3 to score up to 15`() {
        val fivePerms = buildReport(dangerousPermissions = List(5) { "android.permission.PERM_$it" })
        assertEquals(15, fivePerms.riskScore)

        // More than 5 permissions should still be capped at 15
        val tenPerms = buildReport(dangerousPermissions = List(10) { "android.permission.PERM_$it" })
        assertEquals(15, tenPerms.riskScore)
    }

    @Test
    fun `two dangerous permissions add 6 to score`() {
        val report = buildReport(dangerousPermissions = listOf("p1", "p2"))
        assertEquals(6, report.riskScore)
    }

    @Test
    fun `exported components add 2 each up to 10 total`() {
        val report = buildReport(
            exportedActivities = listOf("A"),
            exportedServices = listOf("S"),
            exportedReceivers = listOf("R"),
            exportedProviders = listOf("P")
        )
        // 4 exported components * 2 = 8
        assertEquals(8, report.riskScore)
    }

    @Test
    fun `exported component penalty is capped at 10`() {
        val report = buildReport(
            exportedActivities = List(6) { "Activity$it" }
        )
        // 6 * 2 = 12 but capped at 10
        assertEquals(10, report.riskScore)
    }

    @Test
    fun `no network security config adds 5 to score`() {
        val report = buildReport(networkSecurityConfigPresent = false)
        assertEquals(5, report.riskScore)
    }

    @Test
    fun `score with all risk factors equals 90 and never exceeds 100`() {
        // debuggable(30) + backup(10) + testOnly(10) + minSdk<23(10)
        // + perms(15 capped) + exported(10 capped) + noNetConfig(5) = 90
        val report = buildReport(
            isDebuggable = true,
            allowsBackup = true,
            isTestOnly = true,
            minSdkVersion = 16,
            dangerousPermissions = List(10) { "p$it" },
            exportedActivities = List(6) { "A$it" },
            networkSecurityConfigPresent = false
        )
        assertEquals(90, report.riskScore)
        assertTrue(report.riskScore <= 100)
    }

    // ── Risk level ────────────────────────────────────────────────────────────

    @Test
    fun `risk level is LOW when score below 20`() {
        assertEquals(SecurityReport.RiskLevel.LOW, buildReport().riskLevel)
    }

    @Test
    fun `risk level is MEDIUM when score is 20 to 39`() {
        // allowsBackup(10) + testOnly(10) = 20
        val report = buildReport(allowsBackup = true, isTestOnly = true)
        assertEquals(SecurityReport.RiskLevel.MEDIUM, report.riskLevel)
    }

    @Test
    fun `risk level is HIGH when score is 40 or above`() {
        // debuggable(30) + allowsBackup(10) = 40
        val report = buildReport(isDebuggable = true, allowsBackup = true)
        assertEquals(SecurityReport.RiskLevel.HIGH, report.riskLevel)
    }

    // ── Findings ─────────────────────────────────────────────────────────────

    @Test
    fun `safe app findings contain no-issues message`() {
        val findings = buildReport().findings()
        assertTrue(findings.any { it.contains("No obvious security issues") })
    }

    @Test
    fun `debuggable app findings mention debuggable`() {
        val findings = buildReport(isDebuggable = true).findings()
        assertTrue(findings.any { it.contains("debuggable") })
    }

    @Test
    fun `allowBackup app findings mention allowBackup`() {
        val findings = buildReport(allowsBackup = true).findings()
        assertTrue(findings.any { it.contains("Backup") })
    }

    @Test
    fun `testOnly app findings mention test-only`() {
        val findings = buildReport(isTestOnly = true).findings()
        assertTrue(findings.any { it.contains("test-only") })
    }

    @Test
    fun `dangerous permissions are listed in findings`() {
        val findings = buildReport(
            dangerousPermissions = listOf("android.permission.CAMERA")
        ).findings()
        assertTrue(findings.any { it.contains("android.permission.CAMERA") })
    }

    @Test
    fun `low minSdk warning mentions sdk version`() {
        val findings = buildReport(minSdkVersion = 16).findings()
        assertTrue(findings.any { it.contains("16") })
    }

    @Test
    fun `exported activities are listed in findings`() {
        val findings = buildReport(
            exportedActivities = listOf("com.example.SomeActivity")
        ).findings()
        assertTrue(findings.any { it.contains("SomeActivity") })
    }

    @Test
    fun `no network security config finding is present when absent`() {
        val findings = buildReport(networkSecurityConfigPresent = false).findings()
        assertTrue(findings.any { it.contains("Network Security Config") })
    }

    @Test
    fun `no network security config finding is absent when present`() {
        val findings = buildReport(networkSecurityConfigPresent = true).findings()
        assertFalse(findings.any { it.contains("No custom Network Security Config") })
    }
}
